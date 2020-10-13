#!/usr/bin/env bash

kubectl config set-context minikube --namespace=local-mobile
kubectl config get-contexts | grep '*'