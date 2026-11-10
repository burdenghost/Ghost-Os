#!/usr/bin/env bash
set -euo pipefail

export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y --no-install-recommends ca-certificates curl python3 python3-venv

supported=(nmap sqlmap nikto whois dnsutils git curl jq ripgrep python3-venv)
echo '=== ARM64 package candidate check ==='
for package in "${supported[@]}"; do
  candidate="$(apt-cache policy "$package" | awk '/Candidate:/ {print $2; exit}')"
  if [[ -z "$candidate" || "$candidate" == '(none)' ]]; then
    echo "FAIL: no ARM64 Debian candidate for $package"
    exit 1
  fi
  echo "PASS: $package candidate=$candidate"
done

echo '=== Dependency resolver simulation ==='
apt-get install --simulate --no-install-recommends "${supported[@]}"

echo '=== Actual package installation ==='
apt-get install -y --no-install-recommends "${supported[@]}"

echo '=== Executable smoke tests ==='
nmap --version
sqlmap --version
nikto -Version
whois --version || whois -h whois.iana.org
dig -v
git --version
curl --version
jq --version
rg --version
python3 --version

echo '=== Nmap TCP connect scan smoke test ==='
nmap -sT -Pn -n -p 1 127.0.0.1

echo '=== OSRFramework Python dependency dry run ==='
python3 -m venv /tmp/osrframework-venv
/tmp/osrframework-venv/bin/python -m pip install --upgrade pip
if /tmp/osrframework-venv/bin/python -m pip install --dry-run OSRFramework; then
  echo 'PASS: OSRFramework pip dependency resolution completed on ARM64.'
else
  echo 'BLOCKED: OSRFramework dependencies do not resolve cleanly on this ARM64 Debian/Python baseline.'
  exit 1
fi

echo '=== Metasploit package availability ==='
candidate="$(apt-cache policy metasploit-framework | awk '/Candidate:/ {print $2; exit}')"
if [[ -z "$candidate" || "$candidate" == '(none)' ]]; then
  echo 'BLOCKED: metasploit-framework is not provided by the configured Debian stable repositories.'
else
  echo "Candidate available: $candidate"
  apt-get install --simulate --no-install-recommends metasploit-framework
fi

echo '=== Wireshark CLI availability (not packet-capture capability) ==='
candidate="$(apt-cache policy tshark | awk '/Candidate:/ {print $2; exit}')"
if [[ -z "$candidate" || "$candidate" == '(none)' ]]; then
  echo 'BLOCKED: tshark package has no Debian stable candidate.'
else
  apt-get install --simulate --no-install-recommends tshark
  echo 'NOTE: package availability does not grant Android packet-capture permissions.'
fi

echo 'ARM64 security-tool dependency tests completed.'
