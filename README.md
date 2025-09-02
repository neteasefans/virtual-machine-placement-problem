# An iterated local search for virtual machine placement problem

The 1800 benchmark instances used in our draft titled with "An iterated local search for virtual machine placement problem" were friendly provided in the PACO-VMP literature [refer to [3]]. To facilitate the further research, we upload the instances here. 

The optimal solution certificates are given in 'solutions' directory.

The source code given in 'src' directory implements the proposed CCILS method described in our paper.

** Instructions to use the source code of CCILS

*** To compile:

q.zhou$ javac LocalSearch.java PM.java VM.java

q.zhou$

*** To run: 

q.zhou$ java -cp . LocalSearch Instances

(where -cp . indicates the current working directory, LocalSearch includes the main function, and Instances is the instance directory.)

Reference papers to the virtual machine placement problem (VMP):

[1].Xiao-Fang Liu, Zhi-Hui Zhan, Jeremiah D Deng, Yun Li, Tianlong Gu, and Jun Zhang. An Energy Efficient Ant Colony System for Virtual Machine Placement in Cloud Computing.
IEEE Transactions on Evolutionary Computation, 22(1):113–128, 2016.

[2].Abdelaziz Said Abohamama and Eslam Hamouda. A hybrid energy–aware virtual machine placement algorithm for cloud environments. Expert Systems with Applications, 150:113306, 2020.

[3].Joshua Peake, Martyn Amos, Nicholas Costen, Giovanni Masala, and Huw Lloyd. PACO-VMP: parallel ant colony optimization for virtual machine placement. Future Generation Computer Systems, 129:174–186, 2022.


