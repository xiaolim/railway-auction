package railway.sim;

import java.awt.Desktop;
// import java.io.ByteArrayInputStream;
// import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
// import java.io.ObjectInputStream;
// import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;
// import java.util.stream.Collectors;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

// I know. I know. But native Java does not have a better option :(
import javafx.util.Pair;

// Perhaps use an already available structure?
class Coordinates {
    public int x;
    public int y;
}

public class Simulator {
    private static final String root = "railway";
    private static final String statics_root = "statics";

    private static boolean gui = false;
    private static long timeout = 1;
    private static double fps = 1;

    // Files - hardcoded.
    private static String geo_f = "geography";
    private static String transit_f = "transit";
    private static String infra_f = "infrastructure";
    private static String dir = "railway/sim/input/";

    private static Map<String, Pair<Integer, Integer>> geo;
    private static List<List<Integer>> infra;
    private static int[][] transit;
    private static List<String> townLookup;

    private static List<String> playersNames;
    private static List<PlayerWrapper> players;

    private static List<BidInfo> allBids;

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        parseArgs(args);

        players = new ArrayList<>();
        try {
            for (String name : playerNames) {
                players.add(loadPlayerWrapper(name, timeout));
            }
        } catch (Exception ex) {
            System.out.println("Unable to load players. " + ex.getMessage());
            System.exit(0);
        }

        HTTPServer server = null;
        if (gui) {
            server = new HTTPServer();
            Log.record("Hosting HTTP Server on " + server.addr());
            if (!Desktop.isDesktopSupported())
                Log.record("Desktop operations not supported");
            else if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
                Log.record("Desktop browse operation not supported");
            else {
                try {
                    Desktop.getDesktop().browse(new URI("http://localhost:" + server.port()));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }

        initBids();
        double budget = getBudget();
        for (PlayerWrapper pw : players) {
            pw.init(budget);
        }

        boolean isComplete = false;

        try {
            while (!isComplete) {
                int nullBids = 0;
                for (PlayerWrapper pw : players) {
                    Bid bid = pw.getBid(allBids);
                    if (bid != null) {
                        updateBids(bid);
                    }
                    else {
                        ++nullBids;
                    }
                }

                if (nullBids == players.size()) {
                    isComplete = true;
                }

                if (gui) {
                    gui(server, state(isComplete ? -1 : fps));
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception! " + ex.getMessage());
            System.exit(0);
        }

        printStats();

        System.exit(0);
    }

    public static void printStats() {
        System.out.println("\n******** Results ********");
    }

    private static void initBids() {
        int id = 0;

        for (int i=0; i < infra.size(); ++i) {
            for (int j=0; j < infra.get(i).size(); ++j) {
                BidInfo bi = new BidInfo();
                bi.id = id;

                int t1 = i;
                int t2 = infra.get(i).get(j);
                bi.town1 = townLookup.get(t1);
                bi.town2 = townLookup.get(t2);

                double dist = Math.pow(
                    Math.pow(geo.get(t1).getKey() - geo.get(t2).getKey(), 2) + 
                        Math.pow(geo.get(t1).getValue() - geo.get(t2).getValue(), 2), 
                    0.5);

                bi.amount = transit[t1][t2] * dist * 10;

                List<BidInfo> dups = getDuplicateLinks(townLookup.get(t1), townLookup.get(t2));
                if (dups != null && dups.size() > 0) {
                    int c_size = dups.size();
                    int new_size = c_size + 1;

                    for (BidInfo d : dups) {
                        d.amount = d.amount * c_size / new_size;
                    }

                    bi.amount /= new_size;
                }

                allBids.add(bi);
            }
        }
    }

    private List<BidInfo> getDuplicateLinks(String t1, String t2) {
        List<BidInfo> dups = new ArrayList<>();
        for (BidInfo a : allBids) {
            if (a.town1.equals(t1) && a.town2.equals(t2)) {
                dups.add(a);
            }
        }

        return dups;
    }

    private static double getBudget() {
        int g = players.size();

        double totalAmount = 0;
        for (BidInfo bi : allBids) {
            totalAmount += bi.amount;
        }

        totalAmount *= 2;

        return totalAmount/g;
    }

    private static void loadInputFiles() {
        // Process geo.
        String path = dir + geo_f;
        File file = new File(path);
        Scanner sc = new Scanner(file);
        int index = 0;
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] res = line.split(",");

            townLookup.put(index, res[0]);
            geo.add(new Pair<>(res[1], res[2]));
            infra.add(new ArrayList<String>());
            index += 1;
        }

        // Process infrastructure.
        path = dir + infra_f;
        file = new File(path);
        sc = new Scanner(file);
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] res = line.split(",");
            infra.get(townLookup.get(res[0])).add(res[1]);
            // infra.get(townLookup.get(res[1])).add(res[0]);
        }

        transit = new int[index][index];

        // Process transit.
        // How to store??
        path = dir + transit_f;
        file = new File(path);
        sc = new Scanner(file);
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] res = line.split(",");
            transit[townLookup.get(res[0])][townLookup.get(res[1])] = Integer.parseInt(res[2]);
            // transit[townLookup.get(res[1])][townLookup.get(res[0])] = Integer.parseInt(res[2]);
        }
    }

    private static List<List<Integer>> getClone(List<List<Integer>> lol) {
        List<List<Integer>> newLol = new ArrayList<>();
        for (List<Integer> l : lol) {
            newLol.add(new ArrayList<Integer>(l));
        }

        return newLol;
    }

    // private static Game deepClone(Object object) {
    //     try {
    //         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    //         ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
    //         objectOutputStream.writeObject(object);
    //         ByteArrayInputStream bais = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    //         ObjectInputStream objectInputStream = new ObjectInputStream(bais);
    //         return (Game) objectInputStream.readObject();
    //     }
    //     catch (Exception e) {
    //         e.printStackTrace();
    //         return null;
    //     }
    // }

    private static int[] getScores() {
        throw new UnsupportedOperationException();
    }

    private static PlayerWrapper loadPlayerWrapper(String name, double timeout) throws Exception {
        Log.record("Loading player " + name);
        Player p = loadPlayer(name);
        if (p == null) {
            Log.record("Cannot load player " + name);
            System.exit(1);
        }

        return new PlayerWrapper(p, name, timeout);
    }

    // The state that is sent to the GUI. (JSON)
    private static String state(double fps) {
        throw new UnsupportedOperationException();
    }

    private static void gui(HTTPServer server, String content) {
        if (server == null) return;
        String path = null;
        for (;;) {
            for (;;) {
                try {
                    path = server.request();
                    break;
                } catch (IOException e) {
                    Log.record("HTTP request error " + e.getMessage());
                }
            }
            if (path.equals("data.txt")) {
                try {
                    server.reply(content);
                } catch (IOException e) {
                    Log.record("HTTP dynamic reply error " + e.getMessage());
                }
                return;
            }
            if (path.equals("")) path = "webpage.html";
            else if (!Character.isLetter(path.charAt(0))) {
                Log.record("Potentially malicious HTTP request \"" + path + "\"");
                break;
            }

            File file = new File(statics_root + File.separator + path);
            if (file == null) {
                Log.record("Unknown HTTP request \"" + path + "\"");
            } else {
                try {
                    server.reply(file);
                } catch (IOException e) {
                    Log.record("HTTP static reply error " + e.getMessage());
                }
            }
        }
    }

    private static void parseArgs(String[] args) {
        int i = 0;
        playerNames = new ArrayList<String>();
        for (; i < args.length; ++i) {
            switch (args[i].charAt(0)) {
                case '-':
                    if (args[i].equals("-p") || args[i].equals("--players")) {
                        while (i + 1 < args.length && args[i + 1].charAt(0) != '-') {
                            ++i;
                            playerNames.add(args[i]);
                        }
                    } else if (args[i].equals("-g") || args[i].equals("--gui")) {
                        gui = true;
                    } else if (args[i].equals("-l") || args[i].equals("--logfile")) {
                        if (++i == args.length) {
                            throw new IllegalArgumentException("Missing logfile name");
                        }
                        Log.setLogFile(args[i]);
                    } else if (args[i].equals("-t") || args[i].equals("--timeout")) {
                        if (++i == args.length) {
                            throw new IllegalArgumentException("Missing timeout value.");
                        }

                        timeout = Integer.parseInt(args[i]);
                    } else if (args[i].equals("--fps")) {
                        if (++i == args.length) {
                            throw new IllegalArgumentException("Missing frames per second.");
                        }
                        fps = Double.parseDouble(args[i]);
                    } else if (args[i].equals("-v") || args[i].equals("--verbose")) {
                        Log.activate();
                    } else {
                        throw new IllegalArgumentException("Unknown argument '" + args[i] + "'");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument '" + args[i] + "'");
            }
        }

        if (playerNames.size() == 0) {
            // Set all groups by default.
            playerNames = new ArrayList(["g1", "g2", "g3", "g4", "g5", "g6", "g7", "g8"]);
        }

        Log.record("Players: " + playerNames.toString());
        Log.record("GUI " + (gui ? "enabled" : "disabled"));

        if (gui)
            Log.record("FPS: " + fps);
    }

    private static Set<File> directory(String path, String extension) {
        Set<File> files = new HashSet<File>();
        Set<File> prev_dirs = new HashSet<File>();
        prev_dirs.add(new File(path));
        do {
            Set<File> next_dirs = new HashSet<File>();
            for (File dir : prev_dirs)
                for (File file : dir.listFiles())
                    if (!file.canRead()) ;
                    else if (file.isDirectory())
                        next_dirs.add(file);
                    else if (file.getPath().endsWith(extension))
                        files.add(file);
            prev_dirs = next_dirs;
        } while (!prev_dirs.isEmpty());
        return files;
    }

    public static Player loadPlayer(String name) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String sep = File.separator;
        Set<File> player_files = directory(root + sep + name, ".java");
        File class_file = new File(root + sep + name + sep + "Player.class");
        long class_modified = class_file.exists() ? class_file.lastModified() : -1;
        if (class_modified < 0 || class_modified < last_modified(player_files) ||
                class_modified < last_modified(directory(root + sep + "sim", ".java"))) {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null)
                throw new IOException("Cannot find Java compiler");
            StandardJavaFileManager manager = compiler.
                    getStandardFileManager(null, null, null);
//            long files = player_files.size();
            Log.record("Compiling for player " + name);
            if (!compiler.getTask(null, manager, null, null, null,
                    manager.getJavaFileObjectsFromFiles(player_files)).call())
                throw new IOException("Compilation failed");
            class_file = new File(root + sep + name + sep + "Player.class");
            if (!class_file.exists())
                throw new FileNotFoundException("Missing class file");
        }
        ClassLoader loader = Simulator.class.getClassLoader();
        if (loader == null)
            throw new IOException("Cannot find Java class loader");
        @SuppressWarnings("rawtypes")
        Class raw_class = loader.loadClass(root + "." + name + ".Player");
        return (Player)raw_class.newInstance();
    }

    private static long last_modified(Iterable<File> files) {
        long last_date = 0;
        for (File file : files) {
            long date = file.lastModified();
            if (last_date < date)
                last_date = date;
        }
        return last_date;
    }
}
