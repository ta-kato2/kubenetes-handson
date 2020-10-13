#!/usr/bin/env bash

kubectl config set-context minikube --namespace=local-local-harbor-system
kubectl config get-contexts | grep '*'