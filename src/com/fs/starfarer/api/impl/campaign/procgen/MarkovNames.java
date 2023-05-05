package com.fs.starfarer.api.impl.campaign.procgen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class MarkovNames {

	public static String UNUSED = "*";
	public static String START = "@";
	public static String END = "#";
	
	public static float [][] prob;

	public static String alphabet = UNUSED + START + END + "abcdefghijklmnopqrstuvwxyz-' ";
	public static Map<String, Integer> stringToIndex = new HashMap<String, Integer>();
	public static Map<Integer, String> indexToString = new HashMap<Integer, String>();
	public static int order = 2;
	
	public static void loadIfNeeded() {
		if (prob == null) {
			List<String> names = new ArrayList<String>();
			Collection<NameGenData> specs = Global.getSettings().getAllSpecs(NameGenData.class);
			for (NameGenData spec : specs) {
				//if ((float) Math.random() > 0.1f) continue;
				names.add(spec.getName());
			}
			MarkovNames.load(names, 3);
		}
	}
	
	public static void load(List<String> names, int order) {
		MarkovNames.order = order;
		clear();
		
		List<String> all = new ArrayList<String>();
		for (int i = 1; i < alphabet.length(); i++) {
			String s1 = "" + alphabet.charAt(i);
			all.add(s1);
			for (int j = 1; j < alphabet.length(); j++) {
				String s2 = "" + alphabet.charAt(j);
				all.add(s1 + s2);
				
				if (order == 3) {
					for (int k = 1; k < alphabet.length(); k++) {
						String s3 = "" + alphabet.charAt(k);
						all.add(s1 + s2 + s3);
					}
				}
			}
		}
		for (String str : all) {
			Integer index = computeIndex(str);
			stringToIndex.put(str, index);
			indexToString.put(index, str);
		}
		
		int size = (int) Math.pow(alphabet.length() + 1, order) + (int) Math.pow(alphabet.length(), order - 1);
		
		prob = new float [size][alphabet.length()];
		
		
		for (String name : names) {
			if (name.contains("$")) continue;
			if (containsWord(name, "field")) continue;
			if (containsWord(name, "disk")) continue;
			if (containsWord(name, "asteroid")) continue;
			if (containsWord(name, "nebula")) continue;
			if (containsWord(name, "magnetic")) continue;
			if (containsWord(name, "star")) continue;
			if (containsWord(name, "planet")) continue;
			if (containsWord(name, "world")) continue;
			load(name);
		}
		
		int x = 0;
	}
	
	public static void load(String name) {
		name = START + name + END;
		for (int i = 0; i < name.length() - 1; i++) {
			String curr = "" + name.charAt(i);
			if (i > 0) curr = "" + name.charAt(i - 1) + curr;
			if (i > 1 && order == 3) curr = "" + name.charAt(i - 2) + curr;
			
			String next = "" + name.charAt(i + 1);
			
			int x = getIndex(curr.toLowerCase());
			int y = getIndex(next.toLowerCase());
			
			prob[x][y]++;
		}
	}
	
	public static boolean sanityCheck(String name) {
		if (name.length() <= 2) return false;
		if (name.length() > 20) return false;
		
		int maxUnbroken = 0;
		int currUnbroken = 0;
		int numSpaces = 0;
		for (int i = 0; i < name.length(); i++) {
			String curr = "" + name.charAt(i);
			if (" ".equals(curr) || "-".equals(curr)) {
				currUnbroken = 0;
			}
			if (" ".equals(curr)) {
				numSpaces++;
			}
			
			currUnbroken++;
			if (currUnbroken > maxUnbroken) {
				maxUnbroken = currUnbroken;
			}
		}
		
		if (numSpaces >= 2) return false;
		if (maxUnbroken >= 12) return false;
		
		if (containsBad(name)) return false;
		
		return true;
	}
	
	public static class MarkovNameResult {
		public String name;
		public double probability;
	}
	
	public static MarkovNameResult generate(Random random) {
		if (prob == null) return null;
		if (random == null) random = new Random();
		
		double probability = 1f;
		
		String name = "";
		String add = START;
		while (!END.equals(add) && add != null) {
			name += add;
			
			String curr = add;
			if (name.length() > 1) {
				curr = "" + name.charAt(name.length() - 2) + curr;
			}
			if (name.length() > 2 && order == 3) {
				curr = "" + name.charAt(name.length() - 3) + curr;
			}
			
			WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
			int index = getIndex(curr);
			if (index >= 0) {
				for (int i = 0; i < prob[index].length; i++) {
					float weight = prob[index][i];
					if (weight > 0) {
						String next = getString(i);
						picker.add(next, weight);
					}
				}
			}
			
			add = picker.pick();
			if (add != null) {
				int pickIndex = picker.getItems().indexOf(add);
				if (pickIndex >= 0) {
					probability *= (double) (picker.getWeight(pickIndex) / picker.getTotal());
				}
			}
			
			
			if (name.length() > 50) break;
		}
		if (add != null) name += add;
		
		
		if (name.startsWith(START)) name = name.substring(1);
		if (name.endsWith(END)) name = name.substring(0, name.length() - 1);
		
		StringBuffer result = new StringBuffer();
		String prev = null;
		for (int i = 0; i < name.length(); i++) {
			String curr = "" + name.charAt(i);
			if (" ".equals(prev) || "-".equals(prev) || prev == null) {
				curr = curr.toUpperCase();
			}
			result.append(curr);
			prev = curr;
		}
		
		name = result.toString();
		if (!sanityCheck(name)) name = null;
		
		if (name == null) return null;
		
		MarkovNameResult r = new MarkovNameResult();
		r.name = name;
		r.probability = probability * 1000000;
		return r;
	}
	
	public static void clear() {
		prob = null;
		stringToIndex.clear();
		indexToString.clear();
	}
	
	public static Integer getIndex(String str) {
		if (!stringToIndex.containsKey(str)) {
			return alphabet.indexOf(" ");
		}
		return stringToIndex.get(str);
	}
	public static String getString(Integer index) {
		if (!indexToString.containsKey(index)) {
			return " ";
		}
		return indexToString.get(index);
	}
	
	public static int computeIndex(String str) {
		int index = 0;
		int radix = alphabet.length();
		
		for (int i = 0; i < str.length(); i++) {
			int val = alphabet.indexOf("" + str.charAt(i));
			if (val < 0) continue;
			
			index += val * Math.pow(radix, i);
			radix++;
		}
		
		return index;
	}
	
	
	public static void main(String[] args) {
		List<String> names = createNames();
		load(names, 3);
		
		
//		for (int i = 0; i < 1000; i++) {
//			MarkovNameResult r = generate(null);
//			//if (r != null && r.name != null && r.probability > 1000 && !names.contains(r.name) && r.name.length() > 4) {
//			if (r != null && r.name != null) {
//				System.out.println(r.name + "\t\t\t\t(" + (int) r.probability + ")");
//			}
//		}
		Set<String> generated = new LinkedHashSet<String>();
		for (int i = 0; i < 20000; i++) {
			MarkovNameResult r = MarkovNames.generate(null);
			if (r == null) continue;
			if (r.name == null) continue;
			if (names.contains(r.name)) continue;
			if (generated.contains(r.name)) continue;
			
			System.out.println(r.name);
			generated.add(r.name);
		}
	}
	
	public static boolean containsWord(String in, String word) {
		String [] split = in.split(" |-");
		for (String curr : split) {
			if (curr.toLowerCase().equals(word.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
		
	public static boolean containsBad(String in) {
		if (blocked.contains(in.toLowerCase())) return true;
		
		String [] split = in.split(" |-");
		for (String curr : split) {
			if (badWords.contains(curr.toLowerCase())) return true;
		}
		
		boolean allBlocked = true;
		for (String curr : split) {
			if (!blocked.contains(curr.toLowerCase())) {
				allBlocked = false;
				break;
			}
		}
		if (allBlocked) return true;
		
		return false;
	}
	
	public static Set<String> badWords = new HashSet<String>();
	public static Set<String> blocked = new HashSet<String>();
	static {
		blocked = new HashSet<String>();
		blocked.add("yes");
		blocked.add("the");
		blocked.add("new");
		blocked.add("old");
		blocked.add("and");
		blocked.add("but");
		blocked.add("for");
		blocked.add("all");
		blocked.add("any");
		blocked.add("are");
		blocked.add("ask");
		blocked.add("etc");
		blocked.add("far");
		blocked.add("few");
		blocked.add("get");
		blocked.add("got");
		blocked.add("him");
		blocked.add("his");
		blocked.add("how");
		blocked.add("inc");
		blocked.add("may");
		blocked.add("non");
		blocked.add("nor");
		blocked.add("not");
		blocked.add("now");
		blocked.add("off");
		blocked.add("one");
		blocked.add("our");
		blocked.add("out");
		blocked.add("own");
		blocked.add("per");
		blocked.add("que");
		blocked.add("saw");
		blocked.add("say");
		blocked.add("sub");
		blocked.add("too");
		blocked.add("try");
		blocked.add("two");
		blocked.add("use");
		blocked.add("lay");
		blocked.add("yet");
		blocked.add("you");
		
		badWords = new HashSet<String>();
		badWords.add("fuck");
		badWords.add("fucks");
		badWords.add("fucker");
		badWords.add("fucking");
		badWords.add("fucked");
		badWords.add("shit");
		badWords.add("shits");
		badWords.add("cunt");
		badWords.add("cunts");
		badWords.add("bitch");
		badWords.add("bitches");
		badWords.add("ass");
		badWords.add("arse");
		badWords.add("arses");
		badWords.add("asses");
		badWords.add("dick");
		badWords.add("dicks");
		badWords.add("cock");
		badWords.add("cocks");
		badWords.add("nigga");
		badWords.add("niggas");
		badWords.add("nigger");
		badWords.add("niggers");
		badWords.add("shitass");
		badWords.add("whore");
		badWords.add("twat");
		badWords.add("twats");
		badWords.add("pussy");
		badWords.add("pussys");
		badWords.add("pussies");
		badWords.add("butt");
		badWords.add("butts");
		badWords.add("anus");
	}
	
	
	public static List<String> createNames() {
		List<String> names = new ArrayList<String>();
		
		names.add("Aelurus");
		names.add("Hera");
		names.add("Ares");
		names.add("Deimos");
		names.add("Phobos");
		names.add("Kydoimos");
		names.add("Makhai");
		names.add("Mysminai");
		names.add("Polemos");
		names.add("Hebe");
		names.add("Alexiares");
		names.add("Anicetus");
		names.add("Eris");
		names.add("Dysnomia");
		names.add("Lethe");
		names.add("Limos");
		names.add("Algos");
		names.add("Neikea");
		names.add("Eleuthia");
		names.add("Enyo");
		names.add("Enyalius");
		names.add("Baraq");
		names.add("Zumurrud");
		names.add("Lithos");
		names.add("Temblor");
		names.add("Baal");
		names.add("Bel");
		names.add("Haddu");
		names.add("Ishkur");
		names.add("Enki");
		names.add("Dilmun");
		names.add("Ki");
		names.add("Nintul");
		names.add("Ninsutu");
		names.add("Ninkasi");
		names.add("Dazimua");
		names.add("Enshagag");
		names.add("Nintu");
		names.add("Ninsar");
		names.add("Ninkurra");
		names.add("Uttu");
		names.add("Isimud");
		names.add("Ishtar");
		names.add("Dumuzi");
		names.add("Gudam");
		names.add("Galatura");
		names.add("Kurjara");
		names.add("Dagon");
		names.add("Belatu");
		names.add("Godiva");
		names.add("Great Dismal");
		names.add("Herzog");
		names.add("Pandemonium");
		names.add("Mulciber");
		names.add("Mammon");
		names.add("Leviathon");
		names.add("Asmodeus");
		names.add("Belphegor");
		names.add("Belial");
		names.add("Berith");
		names.add("Ashtoreth");
		names.add("Sidon");
		names.add("Galera");
		names.add("Moloch");
		names.add("Utopia");
		names.add("Azathoth");
		names.add("Abaddon");
		names.add("Dis");
		names.add("Medea");
		names.add("Crucible");
		names.add("Wyrm");
		names.add("Dantalion");
		names.add("Humility");
		names.add("Romance");
		names.add("Wan");
		names.add("Leeloo");
		names.add("Zhu Que");
		names.add("Ada");
		names.add("Rukh");
		names.add("Zenobia");
		names.add("Labyrinth");
		names.add("Genbu");
		names.add("Qilin");
		names.add("Black Emperor");
		names.add("Myrkheim");
		names.add("Idunn");
		names.add("Draupnir");
		names.add("Ivaldi");
		names.add("Brokr");
		names.add("Gadfly");
		names.add("Eitri");
		names.add("Gullinbursti");
		names.add("Habrok");
		names.add("Jotunheim");
		names.add("Greip");
		names.add("Gjalp");
		names.add("Bardarbunga");
		names.add("Thrivaldi");
		names.add("Zosimos");
		names.add("$parent's Wheel");
		names.add("Ezekial's Wheel");
		names.add("Arc of $parent");
		names.add("$parent's Wreath");
		names.add("Torq of $parent");
		names.add("Crown of $parent");
		names.add("Ring System");
		names.add("Accretion Disk");
		names.add("The Ouroboros");
		names.add("$parent's Girdle");
		names.add("Iron Stream");
		names.add("Stone River");
		names.add("The Glittering Band");
		names.add("Asteroid Belt");
		names.add("Stone Conclave");
		names.add("Freyja");
		names.add("Brisingamen");
		names.add("Wotan");
		names.add("Draupnir");
		names.add("Hlidskjalf");
		names.add("Ring of Gyges");
		names.add("Fenrir");
		names.add("Gjoll");
		names.add("Sledovik");
		names.add("Sessho-seki");
		names.add("Arkady");
		names.add("Rocannon's World");
		names.add("Baslag");
		names.add("Boone");
		names.add("Zakalwe");
		names.add("Darkense");
		names.add("Livueta");
		names.add("Elethiomel ");
		names.add("Staberinde");
		names.add("Cache");
		names.add("Ged");
		names.add("Palaver");
		names.add("Rigamarole");
		names.add("Scrimshaw");
		names.add("Hadria");
		names.add("Rosas");
		names.add("Orobas");
		names.add("Murmur");
		names.add("Asmoday");
		names.add("Marax");
		names.add("Amon");
		names.add("Komarov");
		names.add("Nibiru");
		names.add("Brahe");
		names.add("Perseus");
		names.add("Cerenkov");
		names.add("Cygnus");
		names.add("Saurus");
		names.add("Pisces");
		names.add("Carina");
		names.add("Draco");
		names.add("Elysia");
		names.add("Irkalla");
		names.add("Meropis");
		names.add("Nysa");
		names.add("Paititi");
		names.add("Suddene");
		names.add("Yomi");
		names.add("Baltia");
		names.add("Union");
		names.add("Jeremiah's World");
		names.add("Strathcona");
		names.add("Tamsin");
		names.add("Kete");
		names.add("Hrosa");
		names.add("Aether");
		names.add("Eros");
		names.add("Asphodel");
		names.add("Erebus");
		names.add("Nyx");
		names.add("Acheron");
		names.add("Kharon");
		names.add("Tartarus");
		names.add("Khthon");
		names.add("Bothros");
		names.add("Megaron");
		names.add("Thanatos");
		names.add("Hypnos");
		names.add("Morpheus");
		names.add("Phobetor");
		names.add("Phantasos");
		names.add("Oneiroi");
		names.add("Lethe");
		names.add("Cocytus");
		names.add("Oceanus Nebula");
		names.add("Elysion");
		names.add("Minos");
		names.add("Khora");
		names.add("Mnemosyne");
		names.add("Calliope");
		names.add("Myhdon");
		names.add("Edonus");
		names.add("Biston");
		names.add("Kleio");
		names.add("Hyacinth ");
		names.add("Euterpe");
		names.add("Rhesus");
		names.add("Erato");
		names.add("Melpomene");
		names.add("Polymnia");
		names.add("Aglaope");
		names.add("Leucosia");
		names.add("Ligeia");
		names.add("Molpe");
		names.add("Pathenope");
		names.add("Peisinoe");
		names.add("Thelxiope");
		names.add("Terpsichore");
		names.add("Thalia");
		names.add("The Korybantes");
		names.add("Urania");
		names.add("Linus");
		names.add("Dia");
		names.add("Helicon");
		names.add("Aoide");
		names.add("Melete");
		names.add("Mneme");
		names.add("Parnassos");
		names.add("The Banshee");
		names.add("The Sirens");
		names.add("Orpheus");
		names.add("Tragedy");
		names.add("The Kuretes");
		names.add("Mangindusa");
		names.add("Bugawasin");
		names.add("Polo");
		names.add("Sedumunadoc");
		names.add("Tabiacoud");
		names.add("Anggugru");
		names.add("Balugu");
		names.add("Kalabagang");
		names.add("Taliyakad");
		names.add("Langit");
		names.add("Basad");
		names.add("Sidpan");
		names.add("Dibuwat");
		names.add("Kavacha");
		names.add("Orm");
		names.add("Ogion");
		names.add("Ring of Morred");
		names.add("Selidor");
		names.add("Nug");
		names.add("Yeg");
		names.add("Rhogog");
		names.add("Hastur");
		names.add("Yig");
		names.add("Rama");
		names.add("Hanuman");
		names.add("Vanara");
		names.add("Surasa");
		names.add("Vasuki");
		names.add("The Nagas");
		names.add("Kaliya");
		names.add("Manasa");
		names.add("Bakunawa");
		names.add("Karkotaka");
		names.add("Mucalinda");
		names.add("Padmavati");
		names.add("Pearl River");
		names.add("Zmey");
		names.add("Lamya");
		names.add("Smok");
		names.add("Stygia");
		names.add("Cimmeria");
		names.add("Magen");
		names.add("Zirnitra");
		names.add("Arkona");
		names.add("Ved");
		names.add("Kolobok");
		names.add("Mokosh");
		names.add("Perun");
		names.add("Morana");
		names.add("Veles");
		names.add("Jarilo");
		names.add("Morana");
		names.add("Svarog");
		names.add("Dazbog");
		names.add("Indrik");
		names.add("Stribog");
		names.add("Gamayun");
		names.add("Sirin");
		names.add("Alkonost");
		names.add("The Simargl");
		names.add("Baba Yaga");
		names.add("Buyan");
		names.add("Koschei");
		names.add("Kitezh");
		names.add("Neter");
		names.add("Pontus");
		names.add("Tiamat");
		names.add("Apsu");
		names.add("Lotan");
		names.add("Basmu");
		names.add("Usumgallu");
		names.add("Masmahhu");
		names.add("Mushussu");
		names.add("Lahmu");
		names.add("Ugallu");
		names.add("Uridimmu");
		names.add("Girtablullu");
		names.add("Umu Dabrutu");
		names.add("Kulullu");
		names.add("Kusarikku");
		names.add("Yaw");
		names.add("Rahab");
		names.add("Tannin");
		names.add("Anzu");
		names.add("Ausir");
		names.add("Horos");
		names.add("Nekhen");
		names.add("Anapa");
		names.add("Atuan");
		names.add("Tenar");
		names.add("Kossil");
		names.add("Rota Fortunae");
		names.add("Siege Perilous");
		names.add("World Mill");
		names.add("Ara");
		names.add("Durer's Star");
		names.add("Melancholia");
		names.add("Moskva");
		names.add("Griseus");
		names.add("Breq");
		names.add("Hungry Ghost");
		names.add("Diyu");
		names.add("Yama");
		names.add("Naihe Qiao");
		names.add("Youdu");
		names.add("Qinguang");
		names.add("Bing Diyu");
		names.add("Chujiang");
		names.add("Heisheng Dadi");
		names.add("Songdi");
		names.add("Wuguan");
		names.add("Xuechi");
		names.add("Yanluo");
		names.add("Diancheng");
		names.add("Jiaohuan");
		names.add("Taishan");
		names.add("Roujiang");
		names.add("Dushi");
		names.add("Menguo");
		names.add("Pingden");
		names.add("Avici");
		names.add("Jigoku");
		names.add("Enma");
		names.add("Gargoyle");
		names.add("Familiar");
		names.add("Schrat");
		names.add("Kobold");
		names.add("Youming");
		names.add("Yinjian");
		names.add("Soleyn");
		names.add("Glowan");
		names.add("Sagan");
		names.add("Robinson's Moon");
		names.add("Idir");
		names.add("Morthanveld");
		names.add("Issorile");
		names.add("Azad");
		names.add("Gormenghast");
		names.add("Knole");
		names.add("Carcassonne");
		names.add("Sark");
		names.add("Rameumptom");
		names.add("Vidur");
		names.add("Jahannam");
		names.add("Zaqqum");
		names.add("Maalik");
		names.add("Haawiyah");
		names.add("Barzakh");
		names.add("Al Nar");
		names.add("Jahim");
		names.add("Jannah");
		names.add("Touba");
		names.add("Araf");
		names.add("Sheol");
		names.add("Rephaim");
		names.add("Perdition");
		names.add("Aaru");
		names.add("Neter-hkertet");
		names.add("Erlig");
		names.add("Karoaglanlar");
		names.add("Karakizlar");
		names.add("Karash");
		names.add("Matyr");
		names.add("Shyngay");
		names.add("Komur");
		names.add("Badysh");
		names.add("Yabash");
		names.add("Temir");
		names.add("Uchar");
		names.add("Kerey");
		names.add("Ulgan");
		names.add("Maidere");
		names.add("The Ordog");
		names.add("Yav");
		names.add("Prav");
		names.add("Nav");
		names.add("Anyox");
		names.add("Atlin");
		names.add("The Chechidla");
		names.add("Comiaken");
		names.add("Cultus");
		names.add("Gingolx");
		names.add("The Nechako");
		names.add("Tsimshian");
		names.add("Ivavik");
		names.add("Ghost Dance");
		names.add("Mag Mell");
		names.add("Loka");
		names.add("Cipactli");
		names.add("Ehecatl");
		names.add("Spirit Wind");
		names.add("Agiel");
		names.add("Tesh");
		names.add("Rohagi");
		names.add("Cymek");
		names.add("Bered Kai Nev");
		names.add("Samael");
		names.add("Agrat bat Mahlat");
		names.add("Amamar");
		names.add("Naamah");
		names.add("Lilith");
		names.add("Eisheth");
		names.add("Zepar");
		names.add("The Grigori");
		names.add("The Sentinels");
		names.add("Suriel");
		names.add("Old Goat");
		names.add("Kochab");
		names.add("Yaqum");
		names.add("Asbeel");
		names.add("Gadreel");
		names.add("Penemue");
		names.add("Kasdaye");
		names.add("Cambriel Nebula");
		names.add("Armaros' Work");
		names.add("The Myriad");
		names.add("The Lost Legion");
		names.add("Al Kathab");
		names.add("The Armada");
		names.add("Kohnid");
		names.add("The Gengris");
		names.add("New Suroch");
		names.add("Xezbeth");
		names.add("Xaphan");
		names.add("Vanth");
		names.add("The Erinyes");
		names.add("Charun");
		names.add("Calu");
		names.add("Turms");
		names.add("Aita");
		names.add("Anvil");
		names.add("Stray Dog");
		names.add("Valefar");
		names.add("Laverna");
		names.add("Maderakka");
		names.add("Sarakka");
		names.add("Juksakka");
		names.add("Uksakka");
		names.add("Jabme-Akka");
		names.add("Maan-Emo");
		names.add("An");
		names.add("Ki");
		names.add("Anu");
		names.add("Long");
		names.add("Quy");
		names.add("Lan");
		names.add("Phung");
		names.add("Art of the Jinns");
		names.add("The Qarin");
		names.add("Tuchulcha");
		names.add("Suanggi");
		names.add("Surgat");
		names.add("The Imp");
		names.add("Chax");
		names.add("The Raven");
		names.add("The Murder");
		names.add("Saleos");
		names.add("Puloman");
		names.add("Indrani Sachi");
		names.add("Sivasri");
		names.add("The Pontianak");
		names.add("The Pocong");
		names.add("O Tokata");
		names.add("Osor");
		names.add("Empusa");
		names.add("Mormo");
		names.add("Onoskelis");
		names.add("Mara");
		names.add("Krampus");
		names.add("Kroni");
		names.add("Ravanan");
		names.add("Kaliyan");
		names.add("Thuriothanan");
		names.add("Kad");
		names.add("The Marid");
		names.add("Hantu Raya");
		names.add("Tuyul");
		names.add("Koman-tong");
		names.add("Koman-lay");
		names.add("Kwee Kia");
		names.add("Jenglot");
		names.add("Gusoyn");
		names.add("Gallu");
		names.add("Gaap");
		names.add("Dzoavits");
		names.add("Klabautermann");
		names.add("$parent's Jinx");
		names.add("$parent's Hex");
		names.add("The Hecatomb");
		names.add("Grigri");
		names.add("Bakaak");
		names.add("Jumbee's Dance");
		names.add("Kigatilik");
		names.add("Qiqirn");
		names.add("Tupilaq");
		names.add("Tizheruk");
		names.add("Run of the Haietlik");
		names.add("Unhcegila");
		names.add("Okeus");
		names.add("Shapishico");
		names.add("Atahsaya");
		names.add("Wekufe");
		names.add("Gualichu");
		names.add("Babi Ngepet");
		names.add("Royllo");
		names.add("Braxil");
		names.add("Mam");
		names.add("Zuane");
		names.add("Caprica");
		names.add("Letum");
		names.add("Pallida");
		names.add("Somnus");
		names.add("Nox");
		names.add("Mors");
		names.add("The Pactolus");
		names.add("Socordia");
		names.add("Ignavia");
		names.add("Sabriel");
		names.add("Lirael");
		names.add("Clariel");
		names.add("Ranna");
		names.add("Mosrael");
		names.add("Kibeth");
		names.add("Dyrim");
		names.add("Belgaer");
		names.add("Astarael");
		names.add("Saraneth");
		names.add("Tanmar");
		names.add("Maida");
		names.add("Bentusle");
		names.add("Asmaidas");
		names.add("Groclant");
		names.add("Nona");
		names.add("Decima");
		names.add("Morta");
		names.add("Ratna");
		names.add("Sharkara");
		names.add("Valuka");
		names.add("Panka");
		names.add("Dhuma");
		names.add("Tamaha");
		names.add("Mahatamaha");
		names.add("Achuguayo");
		names.add("Teide");
		names.add("Gara");
		names.add("Jonay");
		names.add("Chijoraji");
		names.add("Achuhucanac");
		names.add("Ganigo");
		names.add("Tagoror");
		names.add("Goro");
		names.add("Sose");
		names.add("Faunus");
		names.add("Bona Dea");
		names.add("Pitys");
		names.add("Echo");
		names.add("Kelaineus");
		names.add("Argennon");
		names.add("Aigikoros");
		names.add("Eugeneios");
		names.add("Omester");
		names.add("Daphoenus");
		names.add("Philamnos");
		names.add("Xanthos");
		names.add("Glaukos");
		names.add("Phorbas");
		names.add("Leng");
		names.add("Kang Admi");
		names.add("Mika");
		names.add("Bun Manchi");
		names.add("Migoi");
		names.add("Dzu-teh");
		names.add("Miche");
		names.add("Sarkomand");
		names.add("Cerenerian Sea");
		names.add("Kephalos");
		names.add("Procris");
		names.add("Minyas");
		names.add("Cymene");
		names.add("Arcesius");
		names.add("Astraeus");
		names.add("Boreas");
		names.add("Notus");
		names.add("Eurus");
		names.add("Zephyrus");
		names.add("Toci");
		names.add("Coatlicue");
		names.add("Centeotl");
		names.add("Chicomecoatl");
		names.add("Texcatlipoca");
		names.add("Xantico");
		names.add("Chantico");
		names.add("Mayahue");
		names.add("Mixcoatl");
		names.add("Centzon Huitznauhtin");
		names.add("Tlaloc");
		names.add("The Anillo");
		names.add("Tohil");
		names.add("Denyen");
		names.add("Ekwesh");
		names.add("Peleset");
		names.add("Sherden");
		names.add("Teresh");
		names.add("Weshesh");
		names.add("Tjeker");
		names.add("Bull Moose");
		names.add("Minnow");
		names.add("Will-o-wisp");
		names.add("Stingy Jack");
		names.add("City of Brass");
		names.add("Saraph");
		names.add("Harah");
		names.add("Hazor");
		names.add("Gezor");
		names.add("Ningishzida");
		names.add("Azimua");
		names.add("Nehushtan");
		names.add("Oberon");
		names.add("Cymnea");
		names.add("Faiella");
		names.add("Clarissa");
		names.add("Riga");
		names.add("Sawal");
		names.add("Dara");
		names.add("Mandor");
		names.add("Jurt");
		names.add("Corwin");
		names.add("Merlin");
		names.add("Brand");
		names.add("Jasra");
		names.add("Luke");
		names.add("Random");
		names.add("Morganthe");
		names.add("Martin");
		names.add("Morganna");
		names.add("Moronoe");
		names.add("Mazoe");
		names.add("Gliten");
		names.add("Glitonea");
		names.add("Tyronoe");
		names.add("Thiten");
		names.add("Pomorum");
		names.add("Basilia");
		names.add("Mentonomon");
		names.add("Suroch");
		names.add("Grimnebulin");
		names.add("Weaver");
		names.add("Tesh");
		names.add("Shining Tor");
		names.add("Vannin");
		names.add("Manau");
		names.add("Monabia");
		names.add("Cushag");
		names.add("Ayre");
		names.add("Neb");
		names.add("Dhoo");
		names.add("Keter");
		names.add("Zohar");
		names.add("Binah");
		names.add("Netzach");
		names.add("Yesod");
		names.add("Hod");
		names.add("Simchah");
		names.add("Emet");
		names.add("Grimsnes");
		names.add("Alfur");
		names.add("Grensdalur");
		names.add("Troll");
		names.add("Askja");
		names.add("Vofa");
		names.add("Surtsey");
		names.add("Sal");
		names.add("Krossa");
		names.add("Hvita");
		names.add("Olfusa");
		names.add("Grendel");
		names.add("Modor");
		names.add("The Chiral Wisp");
		names.add("Avice");
		names.add("Maculatus");
		names.add("Deinos");
		names.add("Vuzgrimeti");
		names.add("Balwom");
		names.add("Spira");
		names.add("Zaranya");
		names.add("The Motley");
		names.add("Moile");
		names.add("Prism");
		names.add("Arcus");
		names.add("Welo");
		names.add("Acorn");
		names.add("Hope");
		names.add("Wonder");
		names.add("The Confluence");
		names.add("Cirrus");
		names.add("Olwyn");
		names.add("Pantheon");
		names.add("Eye");
		names.add("Pharos");
		names.add("Tempest");
		names.add("The Precipice");
		names.add("Archon");
		names.add("Sabazios");
		names.add("Terror");
		names.add("Despair");
		names.add("Abandon");
		names.add("Legion");
		names.add("Golgotha");
		names.add("Blost");
		names.add("Uz");
		names.add("Hul");
		names.add("Togar");
		names.add("Mesha");
		names.add("Mu");
		names.add("Naacal");
		names.add("Zahra");
		names.add("Hufra");
		names.add("Zill");
		names.add("Swang");
		names.add("Troano");
		names.add("Mayda");
		names.add("Bolunda");
		names.add("Brazir");
		names.add("Mam");
		names.add("Vlaanderen");
		names.add("Asmaida");
		names.add("Chagos");
		names.add("Eiger");
		names.add("Ran");
		names.add("Vigrid");
		names.add("Hrym");
		names.add("Nog");
		names.add("Gryla");
		names.add("Cormoran");
		names.add("Morvah");
		names.add("Zennor");
		names.add("Madron");
		names.add("Athos");
		names.add("Cadmus");
		names.add("Echion");
		names.add("Laius");
		names.add("The Spartoi");
		names.add("Bellerophon");
		names.add("Cilix");
		names.add("Telephassa");
		names.add("Telephe");
		names.add("Agriope");
		names.add("Shevar");
		names.add("Ferragut");
		names.add("Kroni");
		names.add("Kapre");
		names.add("Zipacna");
		names.add("Mbombo");
		names.add("Basan");
		names.add("Wendigo");
		names.add("Abraxas");
		names.add("Vassago");
		names.add("Ukobach");
		names.add("Valac");
		names.add("Toyol");
		names.add("Solas");
		names.add("Shedim");
		names.add("Raum");
		names.add("Ose");
		names.add("Lempo");
		names.add("Lilin");
		names.add("Killakee Cat");
		names.add("Ipos");
		names.add("Hantu Raya");
		names.add("The Polong");
		names.add("Pelesit");
		names.add("Blue Crow");
		names.add("Marathon");
		names.add("Mazu");
		names.add("Svarga");
		names.add("Sunda");
		names.add("Garbhodaka");
		names.add("Pitrloka");
		names.add("Dalar's Star");
		names.add("Kardashev");
		names.add("Hero");
		names.add("Vecna");
		names.add("Zendar");
		names.add("Yaahl");
		names.add("Sahara");
		names.add("Gobi");
		names.add("Kalahari");
		names.add("Karakum");
		names.add("Sonora");
		names.add("Atacama");
		names.add("Mojave");
		names.add("Namib");
		names.add("Aegea");
		names.add("New Labrador");
		names.add("New Biscay");
		names.add("New Azov");
		names.add("New Cordova");
		names.add("New Pender");
		names.add("New Guayaquil");
		names.add("Third Bogota");
		names.add("Yuendumu");
		names.add("Yulara");
		names.add("Ghan");
		names.add("Warmun");
		names.add("The Ord");
		names.add("Naikoon");
		names.add("Haanas");
		names.add("The Pines");
		names.add("New Caldwell");
		names.add("Potato");
		
		return names;
	}
}







