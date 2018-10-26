compile:
	javac railway/sim/utils/*.java
	javac railway/sim/*.java

clean:
	rm railway/*/*.class

run:
	java railway.sim.Simulator -p g1 g2 g3 g4 g5 g6 g7 g8

map:
	java railway.sim.Simulator --gui -m sim

gui:
	java railway.sim.Simulator --gui --fps 1 -p g1 g2 g3 g4 g5 g6 g7 g8

random:
	java railway.sim.Simulator -p random random random --gui --fps 1
java railway.sim.Simulator -p g4 random random


/home/wolszhang/kaldi-trunk/egs/tedlium/s5_r2/exp/chain_cleaned/tdnn1g_sp_bi/log/train.219.1.log