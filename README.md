まず、すでにminikubeが使用中の人もいるかもなので、一旦minikubeを削除する。
```
minikube delete
```

改めてminikubeを起動する
```
minikube start
```

ターミナルを別途開いて、minikubeのダッシュボードを開く
```
minikube dashboard
```

元のターミナルに戻って、「local-mobile」というネームスペースを作成する

```
kubectl apply -f namespace/namespace.yml
```

現在のネームスペースを確認する

```
kubectl config get-contexts
```

ネームスペースを「minikubeのlocal-mobileに切り替える」切り替え

```
kubectl config set-context minikube --namespace=local-mobile
````

ネームスペースが切り替わったか確認する

```
kubectl config get-contexts
```

これから、この「local-mobile」ネームスペースの中で、いろいろと作ってみます。

まずは、踏み台サーバーのpodを作成する。

```
kubectl apply -f bastion/pod.yml
```

以下のコマンドか、ダッシュボードでpodが出来ていることを確認する。
```
kubectl get pod
```

踏み台サーバーのpodにログインする

```
kubectl exec -it bastion -- bash
```

踏み台サーバーにcurlをインストールする

```
apt update && apt install -y curl
```

一旦踏み台サーバーから抜ける
```
exit
```

次に、locomoco-songのpodを１つ作成する
```
kubectl apply -f song/pod.yml
```

作成されているか以下のコマンドか、ダッシュボードで確認する。IPアドレスも確認する。
```
kubectl get pod -o wide
```

確認できたら、踏み台サーバーからCurlでアクセスしてみる。
```
kubectl exec -it bastion -- bash
curl -i [locomoco-songのIPアドレス]
exit
```

これだとpodが死んだら、もう復活しないので、常に2台のpodをデプロイするように、
デプロイ設定を作成する。

まずは単品で作成したpodを削除する。

```
kubectl delete -f song/pod.yml
```

2台のlocomoco-songをデプロイする設定を適用する。
```
kubectl apply -f song/deployment.yml
```

以下のコマンドか、ダッシュボードで2台のpodが出来ていることを確認する。
```
kubectl get pod
```

試しに１つのpodを削除してみると、しばらくすると復活してくるはず。
```
kubectl delete pod [PODのID]
kubectl get pod
```

デプロイされているlocomoco-songのサーバーがちゃんと動いているかを確認したいので、
踏み台サーバーにログインして、curlで通信してみる。

```
kubectl get pod -o wide
kubectl exec -it bastion -- bash
curl -i [locomoco-songのIPアドレス]
exit
```

これで複数台のlocomoco-songを常時起動できるようになった。
試しに、song/deployment.ymlの設定を変更して、
replicasの値を 2 -> 4 に編集してみる。

そして変更を適用してみる。

```
kubectl apply -f song/deployment.yml
```

以下のコマンドかダッシュボードで確認すると、4台に増えていることが確認できる。
```
kubectl get pod
```

元に戻して、再度適用してみると、
また2台に戻ることが確認できる。


今度は、この2台のlocomoco-songのどちらかに、アクセスできるように、前段にロードバランサーを作成する。
kubenetesでは、serviceと呼ぶ。（正確にはロードバランサーではない）
```
kubectl apply -f song/service.yml
```

踏み台サーバーから、serviceへcurlを叩いてみて、その先のlocomoco-songへ通信されることを確認する。
```
kubectl exec -it bastion -- bash
curl -i locomoco-song-service
exit
```

最後に、minikubeはVM上で起動しているので、VMの外からは通常アクセスが出来ない。
VMの外である、ローカルPCからアクセスできるように、
以下のコマンドを叩いて、serviceにアクセスしてみる。

```
minikube service locomoco-song-service -n local-mobile
```

以上
