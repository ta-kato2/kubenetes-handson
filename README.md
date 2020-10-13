## 初回セットアップ

以下をインストールした状態にしてください。
- virtual box
- kubectl
- minikube
- brew
- helm

### kubectlをインストール
```
brew install kubectl
```

### virtual boxのインストール
以下からDLしてインストールする。
https://www.virtualbox.org/wiki/Downloads

### minikubeをインストールする
```
brew install minikube
```

### helmのインストール
以下でhelmをインストールする。（harborのインストールに必要）
```
brew install helm
```

ついでに以下のhelmリポジトリも追加する。（harborのインストールに必要）
```
helm repo add harbor https://helm.goharbor.io
```

## minikubeを起動する
これからminikubeを起動します。

まず、すでにminikubeが使用中の人もいるかもなので、一旦minikubeを削除する。
```
minikube delete
```

改めてminikubeを起動する。
```
minikube start --vm-driver=virtualbox --insecure-registry local.harbor.dev:80
```

以下のアドオンを追加する。（Ingressの作成やharborに必要）
```
minikube addons enable ingress
minikube addons enable default-storageclass
minikube addons enable registry
```

### minikubeのダッシュボードを開く
別のターミナルを別途開いて、以下のコマンドを実行してください。
ブラウザが起動し、minikubeのダッシュボードが開かれます。

```
minikube dashboard
```

## ハンズオン用に新しくネームスペースを作成する


元のターミナルに戻って、「local-mobile」というネームスペースを作成する。

まずは現在、minikubeを向いていることを確認し、
```
kubectl config get-contexts
```

以下のコマンドで新規にネームスペースを作成します。
```
kubectl apply -f k8s/app/namespace.yml
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

## 動作確認用の踏み台サーバーを作成する

まずは、踏み台サーバーのpodを作成する。

```
kubectl apply -f bastion/pod.yml
```

以下のコマンドか、ダッシュボードでpodが出来ていることを確認する。
```
kubectl get pod
```

### 踏み台サーバーにログインして、curlをインストールする
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

## LocoMoco Songモジュールを作成する（お試しで、直接podを１つ作成する）
次に、locomoco-songのpodを１つ作成する
```
kubectl apply -f k8s/app/song/pod.yml
```

作成されているか以下のコマンドか、ダッシュボードで確認する。IPアドレスも確認する。
```
kubectl get pod -o wide
```

### 踏み台サーバーから、LocoMoco Songへ通信して、Songが正常に起動しているか確認する

踏み台サーバーからCurlでアクセスしてみる。
```
kubectl exec -it bastion -- bash
curl -i [locomoco-songのIPアドレス]
exit
```

### 直接podを作成することの問題点
これだとpodが死んだら、もう復活しない。
そのため、deploymentを作成して、そこからpodが作成されるようにする必要がある。
deploymentを使うと、常に指定した台数のpodが起動するようにすることができる。

### 直接作成したpodを削除してお掃除する
作成したpodを削除する。

```
kubectl delete -f k8s/app/song/pod.yml
```

## LocoMoco Songのデプロイメントを作成して、モジュールを作成する（今度はちゃんとdeploymentからpodを作成する）

2台のlocomoco-songをデプロイする設定を適用する。
```
kubectl apply -f k8s/app/song/deployment.yml
```

以下のコマンドか、ダッシュボードで2台のpodが出来ていることを確認する。
```
kubectl get pod
```

### 試しにpodを削除すると、指定した台数に復旧することを確認する

試しに１つのpodを削除してみると、しばらくすると復活してくるはず。
```
kubectl delete pod [PODのID]
kubectl get pod
```

### 起動したpodが正常に動いているかを確認する
デプロイされているlocomoco-songのサーバーがちゃんと動いているかを確認したいので、
踏み台サーバーにログインして、curlで通信してみる。

```
kubectl get pod -o wide
kubectl exec -it bastion -- bash
curl -i [locomoco-songのIPアドレス]
exit
```

### podの数を変更してみる
これで複数台のlocomoco-songを常時起動できるようになった。
試しに、song/deployment.ymlの設定を変更して、
replicasの値を 2 -> 4 に編集してみる。

そして変更を適用してみる。

```
kubectl apply -f k8s/app/song/deployment.yml
```

以下のコマンドかダッシュボードで確認すると、4台に増えていることが確認できる。
```
kubectl get pod
```

元に戻して、再度適用してみると、
また2台に戻ることが確認できる。


## 複数のpodのどれかにアクセスためのロードバランサー・Serviceを作成する

今度は、この2台のlocomoco-songのどちらかに、アクセスできるように、前段にロードバランサーを作成する。
kubenetesでは、serviceと呼ぶ。（正確にはロードバランサーではない）
```
kubectl apply -f k8s/app/song/service.yml
```

踏み台サーバーから、serviceへcurlを叩いてみて、その先のlocomoco-songへ通信されることを確認する。
```
kubectl exec -it bastion -- bash
curl -i locomoco-song-service
exit
```

## k8s環境の外からアクセスできるように、Ingressを作成する

`http://local.mobile.dev/locomoco-song` で、
minikubeのk8s環境にアクセスした時に、
先程作成した `locomoco-song-service` へアクセスできるようにしたい。

### ローカル環境で名前解決できるようにhostsファイルを修正する

`local.mobile.dev` のドメインを、
ローカル環境にminikubeのk8sクラスタIPアドレスに名前解決できるようにしてあげたいので、
`/etc/hosts` ファイルを修正して、名前解決できるようにする。

まず、minikubeのIPアドレスを以下で確認する。

```
minikube ip
```

次に、hostsファイルを修正する。
```
sudo vi /etc/hosts
```

以下の行を追加して保存する。

```
local.mobile.dev minikubeのIPアドレス
```

### Ingressを作成する
以下のコマンドでingressを作成する。

```
kubectl apply -f k8s/app/song/ingress.yml
```

このingressには、`http://local.mobile.dev/locomoco-song` でアクセスを受けると、
先程作成した `locomoco-song-service` へアクセスできるように設定している。


### ingress経由での外部からアクセスをテストしてみる

`http://local.mobile.dev/locomoco-song` へ
ホストPCからアクセスして、通信ができることを確認する。

※google chromeでアクセスすると、勝手にhttpsに書き換えられて、うまくアクセスできないかもしれない。


---
kubectl apply -f k8s/harbor/namespace.yml
kubectl config set-context minikube --namespace=local-harbor
kubectl config get-contexts

kubectl get node -o wide

上記で確認したInternal-IPを、以下のコマンドに埋め込んで実行する

helm install harbor --namespace local-harbor harbor/harbor \
  --set expose.type=nodePort \
  --set expose.tls.enabled=false \
  --set persistence.enabled=true \
  --set externalURL=http://[Internal-IP]:30002 \
  --set harborAdminPassword=Password1!

kubectl get pods

harbor用のpodたちが起動するまで数分待つ。

外からアクセスできるようにする
kubectl apply -f k8s/harbor/ingress.yml

minikube ip

sudo vi /etc/hosts
以下を追記
```
minikubeのIPアドレス local.harbor.dev
```

http://local.harbor.dev

でharborにアクセス可能。
chromeだと勝手にhttpsになってしまってエラーになるかも。



 docker login local.harbor.dev:80 --username admin
 docker push local.harbor.dev:80/mobile/locomoco-song:latest

以下をJSONの項目に追記して、Docker For Macを再起動する。これをやると、指定したリポジトリはHTTPSじゃなくHTTPでも通るようになる。
vi ~/.docker/daemon.json
  "insecure-registries":["local.harbor.dev:80"]



