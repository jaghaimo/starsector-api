package com.fs.starfarer.api.impl.campaign.intel.bar.events.historian;

import java.util.List;

public class HistorianBackstory {
	public static interface HistorianBackstoryInfo {
		String getText();
		float getWeight();
		String getId();
	}
	
	public static class BaseBackstory implements HistorianBackstoryInfo {
		public String getText() {
			return null;
		}
		public String getId() {
			String name = getClass().getName();
			name = name.substring(name.lastIndexOf('.') + 1);
			return name;
		}
		public float getWeight() {
			return 10;
		}
	}

	public static void init(List<HistorianBackstoryInfo> backstory) {
		// this now lives in rules.csv
/*		
		//final HistorianData hd = HistorianData.getInstance();
		// 1 Historian personal
		backstory.add(new BaseBackstory() {
			@Override
			public String getText() {
				HistorianData hd = HistorianData.getInstance();
				return "\" Sometimes I ponder what my life would be like in the Domain - an uncollapsed Domain? I suspect someone who goes needling " +
				"around asking uncomfortable questions might get in trouble,\" " + hd.getHeOrShe() + " barks a laugh, then lowers " + hd.getHisOrHer() +
				" face. \"Although I've managed to produce enough trouble for myself in the here-and-now, haven't I? If you don't anger the institutions, " +
				"you're not doing good work, I think.";
			}
		});
		
		// 2 Infernium
		backstory.add(new BaseBackstory() {
			@Override
			public String getText() {
				HistorianData hd = HistorianData.getInstance();
				return "\"Oh 'Infernium', yes yes. It's a bit of Luddic vernacular; refers to antimatter starship fuel, though what they mean, " +
				"and here " + hd.getHeOrShe() + " stresses the word, \"is rather more. In certain texts the term encompasses all AM applications, " + 
				"or even all high-energy processes. 'The get of Mammon and Belial', one inventive firebrand called it. I like that one. I've always " +
				"enjoyed the dramatic rhetoric of Luddic folk-preachers.\"";
			}
		});
		
		// 3 Kanta
		backstory.add(new BaseBackstory() {
			@Override
			public String getText() {
				//HistorianData hd = HistorianData.getInstance();
				return "\"Now I may be old, but there's older by far. If the records are to be believed - and I do believe, the Hegemony keeps very good records - " +
						"then the pirate Warlord Kanta was born well before the Collapse. Or shall we call her ex-Domain Armada officer Jorien Kanta? " +
						"Oh yes, she had a name like anyone else once. One wonders if it's very expensive biomods or periodic cryosleep that keeps her going. " +
						"I'd love to find out...\"";
			}
		});
		
		// 4. Andrada
		backstory.add(new BaseBackstory() {
			@Override
			public String getText() {
				HistorianData hd = HistorianData.getInstance();
				return "\"Hmm, history is not all study of 'great leaders', but one can't help but admire a narrative of glorious victory, hubris, and " +
						"ignominious end. Philip Andrada, for instance,\" " + hd.getHisOrHer() + " gaze drifts into the past, \"I remember like it was " +
						"yesterday. He embodied all that was proud and good in the Hegemony. Practically deified on every holo after defeating " +
						"Warlord Loke; oh, how we celebrated at that news! It was the defeat of chaos itself, the dawn of a golden age. So it felt, once.\n\n" +
						"His turn wounded us; it wounded the Hegemony itself, and very deeply.\" " + hd.getUCHeOrShe() + " nods to " + hd.getHimOrHerself() + 
						" and drifts back into some old memory.";
			}
		});
		
		// 5. cryosleep
		backstory.add(new BaseBackstory() {
			@Override
			public String getText() {
				HistorianData hd = HistorianData.getInstance();
				return "\"I've considered cryosleep. Pay the fee, jump ahead a hundred cycles to see how it all turned out. As a historian, "+
				"by the stars, it's quite an appeal. On the other hand - yes, yes - I know the statistics, the failure rates are low nowadays if " +
				"you find a good vault. But it still scares me. You could just go to sleep and never wake up.\" " + hd.getUCHeOrShe() + " laughs darkly, " +
				"\"Though that's every day, isn't it? Ah, I have too much work to do anyway...\"";
			}
		});
		
		// 6 aliens
		backstory.add(new BaseBackstory() {
			@Override
			public String getText() {
				HistorianData hd = HistorianData.getInstance();
				return "\"Aliens! Hah. No, there's nothing well documented enough to be considered,\" " + hd.getHeOrShe() + " stresses this point, " +
						"\"Legitimate History. Yes yes, if you want to experience xenolife, visit Jangala and take your pills and quarantine shower.\"\n\n" +
						"\"But that's simply not what is meant, is it. We want there to be someone else out there like us, someone to talk to, someone to tell us " +
						"we're not alone, someone to give us answers. Someone who cares. Perhaps we're looking in the wrong place? Or thinking in " +
						"the wrong way?\" " + hd.getUCHeOrShe() + " dismisses the thought with a shake of " + hd.getHisOrHer() + " head. \"Eh, " +
						"speculation. Not my trade.\"";
			}
		});
		
		// 7 aliens again
		backstory.add(new BaseBackstory() {
			@Override
			public String getText() {
				HistorianData hd = HistorianData.getInstance();
				return "\"We've all heard the story about the Onslaught battleship, the only thing that can stand against the alien threat.\" " + 
				hd.getHeOrShe() + " scoffs, and waves a dismissive hand, \"As if we live in some milfic holo about brave heroes and vile villains. " +
				"Grand historical context-breaking contact doesn't work that way, at least in our body of knowledge; it's more confusing, " +
				"messy, chaotic. It undermines the entire cognitive-ideological basis of society. And always, so many die.\"\n\n" + hd.getUCHeOrShe() +
				" gets a distant look for a moment, then breaks into a sardonic smile. \"Not so unlike the Collapse eh? Must be aliens.\" " +
				hd.getHeOrShe() + " winks. \"For sure.\"";
			}
		});
		
		// 8 Old Earth
		backstory.add(new BaseBackstory() {
			@Override
			public String getText() {
				HistorianData hd = HistorianData.getInstance();
				return "\"Old Earth, mother Terra. From whence we came, long gone. The Luddics aren't completely wrong in their stories, though " +
						"they, hm, possess few qualms about 'improving' most details to appeal to a quotidian morale framing. The historical record " +
						"is far more complex, far more difficult. Far more relatable in the end, I believe. Every day we survive by powerful " +
						"technologies which any one of us barely understands and which may unleash truly apocalytpic destruction. Mistakes are made, " +
						"tragedy results; it's only a question of scale.\"\n\n" + hd.getUCHeOrShe() + " considers this for a moment, then begins mumbling " +
						"a critical response to " + hd.getHisOrHer() + " own thoughts, \"But scale can indeed fundamentally change the quality of " +
						"phenomena, hence the contextualization of the moral dimension, hmm, entirely! Hmm... \"";
			}
		});
		
		// 9 technology
		backstory.add(new BaseBackstory() {
			@Override
			public String getText() {
				HistorianData hd = HistorianData.getInstance();
				return "\"Though we're freed from the Domain's technology restrictions - despite Hegemony attempts to carry on that legacy,\" " + hd.getHeOrShe() +
						" gives a half shrug to their efficacy at this effort, \"- this age has not seen any great leaps of technological development. " +
						"Why not? The ban has been lifted!\"\n\n" +
						"Perhaps it is that development of highly complex, advanced technologies requires a stable foundation: a large population base with " +
						"excess engineering and theoretical capacity, a willingness to devote resources to pursuits beyond mere survival. Or war. And yes, " +
						"war drives investment of a sort. But imagine the possibilities if billions of credits had not been vaporized. Alongside so many foolish youths " +
						"I once knew. " + hd.getUCHeOrShe() + " goes very quiet for a moment. \"Those old production chips and nanoforges offer a ladder with the " +
						"middle rungs missing. We fight to be the one to reach for the top, doomed to fall.\"";
			}
		});
		
		// 10 the Domain, gates
		backstory.add(new BaseBackstory() {
			@Override
			public String getText() {
				HistorianData hd = HistorianData.getInstance();
				return "\"The Domain split key industries into separate worlds, and yes, with redundancies - set well apart from one another. " +
						"If one world which produces hyperdrives tried to leverage their specialization against the Domain, well, what good are they " +
						"without raw materials? Or AM fuel? Or, indeed, the basic necessities of life and access to markets?\"\n\n" +
						"\"How can one rebel when the Domain need only control the local Gate to lay siege? Which one world could possibly stand against the " +
						"entire Domain?\"" + hd.getUCHeOrShe() + " gives a grim smirk. \"Now we're stuck here, besieged by the Domain. There's no one " + 
						"to accept our surrender but some dead Gates.\"";
			}
		});
		
		// 11 Galatia Academy
		backstory.add(new BaseBackstory() {
			@Override
			public String getText() {
				HistorianData hd = HistorianData.getInstance();
				return "\"The Galatia Academy? Oh yes, yes, I once attended. And taught there too, if you'd believe it. For quite a few cycles. " +
						"They figured me out as a troublemaker soon enough, but I was slippery. I played the game. Me! Hah, would you believe that? " +
						hd.getUCHeOrShe() + " laughs at some old memory. \"Made a few enemies too, I did.\"\n\n" + 
						"\"Did you hear who they put in charge of the Academy? Anahita Baird! Can you believe it? The wolf is tending the sheep now, and that's the " +
						"truth. The Hegemony has no idea what they've done,\" " + hd.getHeOrShe() + " cackles. \"No idea!\"";
			}
		});
		
		// 12 Baikal Daud
		backstory.add(new BaseBackstory() {
			@Override
			public String getText() {
				HistorianData hd = HistorianData.getInstance();
				return "\"With Baikal Daud in the office of High Hegemon, I can't help but wonder at what mark his administration will make on the " +
						"Phoenix of the Persean Sector. The old guard still dominates the navy - that's the Eventide contingent. Those families go back " +
						"to the arrival of the 14th in the Sector. It's not an old aristocracy by any historical standard, but the pattern is " +
						"established quickly enough given the correct conditions. Where was I- yes, Daud. Man of the people. Ruffles feathers. Like when he " +
						"intervened personally in the Sphinx ensigns scandal. Hegemons simply don't do that. But he's a born leader, none can deny it. That's " +
						"what is really going to get him in trouble.\" " + hd.getUCHeOrShe() + " stares out at nothing, thoughtfully.";
			}
		});
		
		// 13 Artemisia Sun
		backstory.add(new BaseBackstory() {
			@Override
			public String getText() {
				HistorianData hd = HistorianData.getInstance();
				return "\"In my circles- that's historians, mind you, not just old folks hanging around the bar,\" " + hd.getHeOrShe() + " chuckles at " +
				hd.getHisOrHer() + " self-deprecation, \"-We've made a rule not to argue about Artemisia Sun unless legitimate new information appears. " +
				"And it never does!\" " + hd.getUCHeOrShe() + " pauses for a moment. \"But you're not a historian, so the rule doesn't apply. Great!\"\n\n" +
				"\"So: Why was she voted off the board just in time for the crucial military disaster of the Second AI War, then able to sweep back into " +
				"her office with the credibility of all who opposed her absolutely annihilated? Was it merely a ruthless recovery, or was it a calculated " +
				"sacrifice of Tri-Tachyon assets in order to consolidate personal power?\" " + hd.getUCHeOrShe() + " smiles. \"Maybe we can hash this theory " +
				"all the way through one of these days.\"";
			}
		});
		
		// 14 Ludd
		backstory.add(new BaseBackstory() {
			@Override
			public String getText() {
				HistorianData hd = HistorianData.getInstance();
				return "\"How we long for some incidental recording of even one of Ludd's speeches or acts. Incredible, is it not, how all evidence seems " +
						"to have disappeared? This, hrm, 'miracle' is of course a point of faith among followers of the Church of Galactic Redemption.\"\n\n" + 
						hd.getUCHeOrShe() + " raises an eyebrow at you. \"And, officially, a point of policy for the Hegemony, which seeks to appease these faithful.\" " +
						hd.getUCHeOrShe() + " furrows " + hd.getHisOrHer() + " brow in frustration, \"As a historian, of course, this vexes me to no end. " +
						"History as experienced by the common person may be a story society tells itself, and distorted as such. Even still! It must be grounded " +
						"in facts!\"\n\n" + 
						"\"Of course if I did discover a holovid of Ludd's Trial, some fanatic would murder me soon enough for releasing it. Not that I could " +
						"help myself.\" " + hd.getUCHeOrShe() + " smiles impishly, then considers. \"If you do ever find one, you'd give me a copy, wouldn't you?\"";
			}
		});
		
		// 15 smuggling
		backstory.add(new BaseBackstory() {
			@Override
			public String getText() {
				HistorianData hd = HistorianData.getInstance();
				return "\"You wouldn't believe how much of my work involves reviewing cargo manifests. I have an old student who works on Agreus " +
						"who sends me copies they lift off scrapped memory cores. Most of the real secrets are wiped, of course, but the story is there, " +
						"between the lines. History is written in logistics!\n\n" + 
						"Take for instance the quantity of food and supplies that disappear en route on trade routes that skim the " +
						"edges of the Core Worlds. A simple statistical sampling suggests that a significant percent of all economic activity in the " +
						"Sector is 'off the books', as they say, and a significant portion of that goes straight to what are called 'pirates' and " +
						"so-called 'decivilized' demographics.\"\n\n " +
						hd.getUCHeOrShe() + " shakes " + hd.getHisOrHer() + " head, smiling. \"Simply astounding.\"";
			}
		});
		
		// 16 the start of the Hegemony
		backstory.add(new BaseBackstory() {
			@Override
			public String getText() {
				HistorianData hd = HistorianData.getInstance();
				return "\"Ah, like I was just telling those young folks over there,\" the historian waves " + hd.getHisOrHer() + " hand toward a small group " +
						" which seems to be conspicuously avoiding eye contact, \"- after the trials of the Cold Passage, the 14th Battlegroup limped across " +
						"the Persean frontiers to discover the Sector dominated by the Warlord Leonis. The joint command of the 14th struck down that cruel " +
						"reign and were hailed as liberators, laying the groundwork for Admiral Kali Molina to make a declaration of Hegemony over the former " +
						"Domain colonies of this Sector with the Eventide Diktat of c49.\n\n"
						+ "The untarnished Hegemony of that time was too overshadowed by memories " +
						"of the Domain to truly appreciate the political capital at the High Hegemon's command. Short-term thinking prevailed in anticipation " +
						"of Gate reactivation. Indeed,\"" + hd.getUCHeOrShe() + " gets a look of sad understanding, \"How could they have known that " +
								"a state of discontinuity would last so long?\"";
			}
		});
		*/
	}
}











