#!/bin/bash
set -eo pipefail

# --- Configuration ---
DOCKER_USERNAME="saadhanp"
DOCKER_REPO_NAME="saadhanp/task-backend"
K8S_DEPLOYMENT_NAME="task-api-deployment"
K8S_CONTAINER_NAME="task-api"

# Use current timestamp as a unique tag
IMAGE_TAG=$(date +%s)
FULL_IMAGE_NAME="$DOCKER_REPO_NAME:$IMAGE_TAG"

echo "--- Starting Task 4 CI/CD Simulation ---"

# -----------------------------------------------------
# 1. CI: CODE BUILD (Maven)
# -----------------------------------------------------
echo "1. Building Java application..."
if ! mvn clean package -DskipTests; then
    echo "ERROR: Maven build failed."
    exit 1
fi
echo "Java JAR built successfully."

# -----------------------------------------------------
# 2. CD: DOCKER BUILD & PUSH
# (This step assumes you have logged into Docker Hub previously)
# -----------------------------------------------------
echo "2. Building Docker image: $FULL_IMAGE_NAME"
if ! docker build -t $FULL_IMAGE_NAME -f Dockerfile .; then
    echo "ERROR: Docker image build failed."
    exit 1
fi
echo "Docker image built locally. Pushing to Docker Hub..."

if ! docker push $FULL_IMAGE_NAME; then
    echo "ERROR: Docker push failed. Check credentials/network."
    exit 1
fi
echo "Image pushed successfully."

# -----------------------------------------------------
# 3. CD: KUBERNETES DEPLOYMENT
# (This step assumes kubectl is configured locally)
# -----------------------------------------------------
echo "3. Updating Kubernetes Deployment: $K8S_DEPLOYMENT_NAME"

# Update image using the unique timestamp tag
kubectl set image deployment/$K8S_DEPLOYMENT_NAME \
    $K8S_CONTAINER_NAME=$FULL_IMAGE_NAME

echo "Waiting for deployment rollout..."
kubectl rollout status deployment/$K8S_DEPLOYMENT_NAME

echo "--- Deployment Complete ---"

    
