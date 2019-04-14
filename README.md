# FEUP-SDIS-Proj

Compiling

    - To compile the program run "make" on the src directory

Running

    - To run the program, you have two options:
    
        - You can run the script tab.sh with "sh tab.sh", that has several methods prepared (the last terminal the script creates is the TestApp), and you just choose the option you want
        
        - Or you can call "java Peer <protocol_version> <peer_id> <access_point> <MC_Address> <MC_Port> <MDB_Address> <MDB_Port> <MDR_Address> <MDR_Port>" to create a Peer and "java TestApp <remote_object_name> <sub_protocol> [<oper_1> <oper_2>]" to run a TestApp command

Used code

    - Enconding functions from https://www.baeldung.com/sha-256-hashing-java
