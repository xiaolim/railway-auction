compile:
	javac railway/sim/utils/*.java
	javac railway/sim/*.java

clean:
	rm railway/*/*.class

run:
	java railway.sim.Simulator

gui:
	java railway.sim.Simulator --gui --fps 1

random:
	java railway.sim.Simulator -p random random random --gui --fps 1
