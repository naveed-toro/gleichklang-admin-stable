#!/usr/bin/env bash

export DEBIAN_FRONTEND=noninteractive
wget https://dev.mysql.com/get/mysql-apt-config_0.8.7-1_all.deb
sudo debconf-set-selections <<< 'mysql-apt-config mysql-apt-config/unsupported-platform select abort'
sudo debconf-set-selections <<< 'mysql-apt-config mysql-apt-config/repo-codename select trusty'
sudo debconf-set-selections <<< 'mysql-apt-config mysql-apt-config/repo-distro select ubuntu'
sudo debconf-set-selections <<< 'mysql-apt-config mysql-apt-config/select-server select mysql-5.6'
sudo debconf-set-selections <<< 'mysql-apt-config mysql-apt-config/select-tools select Enabled'
sudo debconf-set-selections <<< 'mysql-apt-config mysql-apt-config/select-product select Ok'

sudo -E dpkg -i mysql-apt-config_0.8.7-1_all.deb

sudo apt-get update
sudo apt-get -y install vim

sudo debconf-set-selections <<< 'mysql-community-server mysql-community-server/root-pass password root'
sudo debconf-set-selections <<< 'mysql-community-server mysql-community-server/re-root-pass password root'

sudo apt-get -y install mysql-server mysqltuner

sudo cp /vagrant/mysql/vagrant/my.cnf /etc/mysql/my.cnf

mysql -u root -proot < /vagrant/mysql/setup_mysql.sql

sudo service mysql restart
