#!/bin/bash

set -e

echo "Building Docker image..."
docker build -t profile:latest -f Dockerfile .

echo "Loading image into Kind cluster (oms-dev)..."
kind load docker-image profile:latest --name oms-dev

echo "Applying deploymentprofile.yml..."
kubectl apply -f ../k8s/deploymentprofile.yml

echo "Restarting deployment..."
kubectl rollout restart deployment/profile

echo "Waiting for pod..."
sleep 4

echo "Pod status:"
kubectl get pods | grep profile
