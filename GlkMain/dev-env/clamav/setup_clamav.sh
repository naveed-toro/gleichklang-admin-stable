#!/usr/bin/env bash

sudo apt-get -y install clamav-daemon
sudo cp /vagrant/clamav/clamd.conf /etc/clamav/clamd.conf
sudo freshclam
sudo /etc/init.d/clamav-daemon restart
