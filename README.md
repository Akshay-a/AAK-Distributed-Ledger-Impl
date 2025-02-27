This Project is a basic version of implementing a distributed ledger in Java

First Iteration: 

- Two ports run the same program, whenever there is an insert, data gets brodcasted to other port as well
- Whenever a node starts it pulls latest available chain from the network and then starts reading latest blocks.
- Run below java program in 2 different ports:
      java .\Node.java 8081 8080 localhost 8081
       java .\Node.java 8080 8081 localhost 8080



Second Iteration: (TODO)
Add a POW consesus to make the application secure and tamper-resistant. 
A nonce (number used once) is added to the block. This is used in proof-of-work systems (e.g., mining in blockchain) to make tampering computationally expensive.
Refer - MineBlock() 