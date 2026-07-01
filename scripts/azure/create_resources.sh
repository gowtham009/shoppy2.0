#!/usr/bin/env bash
set -euo pipefail

# Usage: RG, LOCATION, ACR, PLAN, WEBAPP can be set as env vars, or accept defaults.
# Example: RG=my-rg LOCATION=eastus ACR=myacr ./scripts/azure/create_resources.sh

RG=${RG:-gkshoppy-rg}
LOCATION=${LOCATION:-eastus}
TIMESTAMP=$(date +%s)
ACR=${ACR:-gkshoppyacr$TIMESTAMP}
PLAN=${PLAN:-gkshoppy-plan}
WEBAPP=${WEBAPP:-gkshoppy-webapp$TIMESTAMP}

echo "Using values:"
echo "  Resource group: $RG"
echo "  Location:       $LOCATION"
echo "  ACR name:       $ACR"
echo "  App plan:       $PLAN"
echo "  Web App name:   $WEBAPP"

echo "\nEnsure you're logged in with 'az login' and have permission to create resources."
read -p "Continue? [y/N] " confirm
if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
  echo "Cancelled." && exit 1
fi

echo "\nCreating resource group..."
az group create --name "$RG" --location "$LOCATION"

echo "\nCreating Azure Container Registry (ACR)..."
az acr create --resource-group "$RG" --name "$ACR" --sku Basic --location "$LOCATION"

ACR_LOGIN_SERVER=$(az acr show --name "$ACR" --resource-group "$RG" --query loginServer -o tsv)
echo "ACR login server: $ACR_LOGIN_SERVER"

echo "\nCreating App Service plan (Linux)..."
az appservice plan create --name "$PLAN" --resource-group "$RG" --is-linux --sku P1V2

echo "\nCreating Web App for Containers (placeholder image)..."
az webapp create --resource-group "$RG" --plan "$PLAN" --name "$WEBAPP" --deployment-container-image-name mcr.microsoft.com/dotnet/aspnet:7.0

SUBSCRIPTION_ID=$(az account show --query id -o tsv)
SCOPE="/subscriptions/$SUBSCRIPTION_ID/resourceGroups/$RG/providers/Microsoft.ContainerRegistry/registries/$ACR"

echo "\nCreating service principal with acrpush role (for GitHub Actions)..."
SP_JSON=$(az ad sp create-for-rbac --name "github-sp-$ACR" --role acrpush --scopes "$SCOPE" --sdk-auth)

echo "Service principal JSON (copy this to GitHub secret AZURE_CREDENTIALS):"
echo "$SP_JSON"
echo "\nAlso save the JSON to ./azure_sp_${ACR}.json"
printf '%s' "$SP_JSON" > "azure_sp_${ACR}.json"

echo "\n(Optionally) enable ACR admin user and show credentials:"
echo "az acr update -n $ACR -g $RG --admin-enabled true"
echo "az acr credential show -n $ACR -g $RG"

echo "\nSummary - add these GitHub secrets in your repo settings:"
echo "  AZURE_CREDENTIALS = (the JSON output above)"
echo "  ACR_NAME = $ACR"
echo "  AZURE_WEBAPP_NAME = $WEBAPP"
echo "  AZURE_RESOURCE_GROUP = $RG"
echo "  VITE_SUPABASE_URL and VITE_SUPABASE_ANON_KEY = your Supabase values"

echo "\nDone. After you add the secrets, push to 'main' to trigger the workflow."
