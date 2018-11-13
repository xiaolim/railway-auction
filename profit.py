
maps = ["g1","g2","g3","g4","g5","g6","g7","g8","fjk","kr","sa"]
excluded = ["g1","g2","g3","g4","g5","g6","g7","g8"]
files = []
for i in range(0,2):
	for m in maps:
		files.append("map_"+m+"_"+str(i))
for f in files:
	res = ""
	with open(f) as inp:
		flag = 0
		print(f)
		for line in inp:
			if flag == 2:
				res += line
			if "******** Results ********" in line:
				flag = 1
			if "Group Profit" in line and flag == 1:
				flag = 2
		print(res)
