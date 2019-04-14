#!/bin/bash

timeout=300; # Time before the tabs close

# Peers
gnome-terminal -x sh -c '((sleep \'$timeout' && kill -9 $$)&); java Peer 2.0 1 peer_1 224.0.0.1 8081 224.0.0.2 8082 224.0.0.3 8083; exec bash'
gnome-terminal -x sh -c '((sleep \'$timeout' && kill -9 $$)&); java Peer 2.0 2 peer_2 224.0.0.1 8081 224.0.0.2 8082 224.0.0.3 8083; exec bash'
gnome-terminal -x sh -c '((sleep \'$timeout' && kill -9 $$)&); java Peer 2.0 3 peer_3 224.0.0.1 8081 224.0.0.2 8082 224.0.0.3 8083; exec bash'
gnome-terminal -x sh -c '((sleep \'$timeout' && kill -9 $$)&); sleep 20s; java Peer 1.0 4 peer_4 224.0.0.1 8081 224.0.0.2 8082 224.0.0.3 8083; exec bash'

# TestApp
#gnome-terminal -x sh -c 'java TestApp localhost peer_1 STATE; exec bash'
#gnome-terminal -x sh -c 'java TestApp localhost peer_2 STATE; exec_bash'
#gnome-terminal -x sh -c 'java TestApp localhost peer_3 STATE; exec bash'
#gnome-terminal -x sh -c '((sleep 30s && kill -9 $$)&); sleep 2s; echo Running: java TestApp localhost peer_3 BACKUP penguin.jpg 1; java TestApp localhost peer_3 BACKUP penguin.jpg 1; exec bash'

gnome-terminal -x sh -c '((sleep \'$timeout' && kill -9 $$)&);
                        sleep 1s;
                        sh testApp.sh
                        exec bash;'



# McastSnooper
# java -jar McastSnooper.jar 224.0.0.1:8081 224.0.0.2:8082 224.0.0.3:8083

# RMI Registry (fernando)
# rmiregistry -Djava.rmi.server.codebase=file:///Desktop/FEUP/SDIS/FEUP-SDIS-Proj

