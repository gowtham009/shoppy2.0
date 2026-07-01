#!/usr/bin/env bash
set -euo pipefail

# Usage: run from repo root. Optionally set env vars RG, LOCATION, VNET, SUBNET, NETWORK_PROFILE
RG=${RG:-gkshoppy-rg}
LOCATION=${LOCATION:-eastus}
VNET=${VNET:-gkshoppy-vnet}
SUBNET=${SUBNET:-gkshoppy-subnet}
NETWORK_PROFILE=${NETWORK_PROFILE:-gkshoppy-netprofile}

TEMPLATE="scripts/azure/networkProfile.json"

if [ ! -f "$TEMPLATE" ]; then
  echo "Template not found: $TEMPLATE"
  echo "Ensure you're running from the repository root where the 'scripts/azure' folder exists."
  exit 1
fi

echo "Using: RG=$RG, LOCATION=$LOCATION, VNET=$VNET, SUBNET=$SUBNET, NETWORK_PROFILE=$NETWORK_PROFILE"

az group create --name "$RG" --location "$LOCATION"

az deployment group create \
  --resource-group "$RG" \
  --template-file "$TEMPLATE" \
  --parameters networkProfileName="$NETWORK_PROFILE" location="$LOCATION" vnetName="$VNET" subnetName="$SUBNET"

echo "Deployment finished."
