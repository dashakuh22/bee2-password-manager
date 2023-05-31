#!/usr/bin/env bash

docker-compose down && docker-compose up -d
sudo docker run -dit --name my-apache-app -p 8080:80 -v "$PWD"/bee2-proj/bee2-password-manager-master-frontend/dist/bee2-master-site:/usr/local/apache2/htdocs/ httpd:2.4
