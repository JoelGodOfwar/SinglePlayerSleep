package com.github.joelgodofwar.sps.util;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.github.joelgodofwar.sps.SinglePlayerSleep;
import com.github.joelgodofwar.sps.common.PluginLogger;

@SuppressWarnings("unused")
public class VersionChecker {
	private String pluginName;
	private JavaPlugin plugin;
	private int projectID;
	private String currentVersion;
	private String githubURL; // = "https://github.com/JoelGodOfwar/NoEndermanGrief/raw/master/versions/1.14/versions.xml";
	String versionType;
	private List<String> releaseList = new ArrayList<>();
	private List<String> developerList = new ArrayList<>();
	private String recommendedVersion = "uptodate";
	private SinglePlayerSleep sps;
	public static PluginLogger LOGGER;

	@SuppressWarnings("static-access")
	public VersionChecker(SinglePlayerSleep plugin, int projectID, String githubURL) {
		this.sps = plugin;
		this.projectID = projectID;
		this.currentVersion = plugin.getDescription().getVersion();
		this.githubURL = githubURL;
		this.LOGGER = new PluginLogger(plugin.getInstance());
	}

	public VersionChecker(String plugin, int projectID, String githubURL) {
		this.currentVersion = plugin;
		this.projectID = projectID;
		this.githubURL = githubURL;
	}

	public String getReleaseUrl() {return "https://spigotmc.org/resources/" + projectID;}

	public boolean checkForUpdates() throws Exception {
		URL url = new URL(githubURL);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		URLConnection connection = url.openConnection();
		connection.setUseCaches(false);
		connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
		Document doc = db.parse(connection.getInputStream());
		doc.getDocumentElement().normalize();
		NodeList releaseNodes = doc.getElementsByTagName("release");
		for (int i = 0; i < releaseNodes.getLength(); i++) {
			Node node = releaseNodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				releaseList.add(element.getElementsByTagName("version").item(0).getTextContent().replace("<version>", "").replace("</version>", ""));
				releaseList.add(element.getElementsByTagName("notes").item(0).getTextContent().replace("<notes>", "").replace("</notes>", ""));
				releaseList.add(element.getElementsByTagName("link").item(0).getTextContent().replace("<link>", "").replace("</link>", ""));
			}
		}
		NodeList developerNodes = doc.getElementsByTagName("developer");
		for (int i = 0; i < developerNodes.getLength(); i++) {
			Node node = developerNodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				developerList.add(element.getElementsByTagName("version").item(0).getTextContent().replace("<version>", "").replace("</version>", ""));
				developerList.add(element.getElementsByTagName("notes").item(0).getTextContent().replace("<notes>", "").replace("</notes>", ""));
				developerList.add(element.getElementsByTagName("link").item(0).getTextContent().replace("<link>", "").replace("</link>", ""));
			}
		}

		if (connection != null) {
			connection.getInputStream().close();
		}
		String releaseVersion = releaseList.get(0);
		String developerVersion = developerList.get(0);
		LOGGER.debug(ChatColor.RED + "currentVersion=" + currentVersion);
		LOGGER.debug(ChatColor.RED + "releaseVersion=" + releaseVersion);
		LOGGER.debug(ChatColor.RED + "developerVersion=" + developerVersion);
		if (currentVersion.compareTo(releaseVersion) < 0) {
			recommendedVersion = "release";
			return true;
		} else if (currentVersion.equals(releaseVersion)) {
			recommendedVersion = "uptodate";
			return false;
		} else if (currentVersion.contains(".D")) {
			String[] splitCurrentVersion = currentVersion.split("\\.D");
			String currentReleaseVersion = splitCurrentVersion[0];
			String[] splitDeveloperVersion = developerVersion.split("\\.D");
			String developerReleaseVersion = splitDeveloperVersion[0];
			if (currentReleaseVersion.equals(releaseVersion) && (developerReleaseVersion.compareTo(currentReleaseVersion) <= 0)) {
				recommendedVersion = "release";
				return true;
			} else if ((developerReleaseVersion.compareTo(releaseVersion) < 0) && developerReleaseVersion.equals(currentReleaseVersion)) {
				recommendedVersion = "You are Up To Date";
				return false;
			} else if (developerVersion.compareTo(currentVersion) > 0) {
				recommendedVersion = "developer";
				return true;
			}
		}

		return false;
	}
	public List<String> getReleaseList() {
		return releaseList;
	}
	public List<String> getDeveloperList() {
		return developerList;
	}
	public String getRecommendedVersion() {
		return recommendedVersion;
	}
	public String oldVersion() {
		return currentVersion;
	}
	public String newVersion() {
		if(recommendedVersion.equalsIgnoreCase("release")) {
			return releaseList.get(0);
		}else if(recommendedVersion.equalsIgnoreCase("developer")) {
			return developerList.get(0);
		}else {
			return "UpToDate";
		}
	}
	public String newVersionNotes() {
		if (recommendedVersion.equalsIgnoreCase("release")) {
			return releaseList.get(1);
		} else if (recommendedVersion.equalsIgnoreCase("developer")) {
			return developerList.get(1);
		} else {
			return "UpToDate";
		}
	}
	public String getDownloadLink() {
		if (recommendedVersion.equalsIgnoreCase("release")) {
			return releaseList.get(2);//"https://www.spigotmc.org/resources/no-enderman-grief2.71236/history";
		} else if (recommendedVersion.equalsIgnoreCase("developer")) {
			return developerList.get(2);//"https://discord.com/channels/654087250665144321/654444482372173875";
		} else {
			return "UpToDate";
		}
	}

	/**public boolean checkForUpdates2() {
		if(currentVersion.contains(".D")){
			versionType = "developer";
		}else{
			versionType = "release";
		}
        String[] versionArray = getLatestVersion(versionType);

        if (versionArray == null) {
        	Bukkit.getLogger().warning(Ansi.RED + "Could not connect to update server.");
            return false;
        }

        String latestVersion = versionArray[0];
        String latestNotes = versionArray[1];

        if (latestVersion.equals(currentVersion)) {
        	return false;
        } else {
            System.out.println("There is a new version of " + pluginName + " available!");
            System.out.println("Your version: " + currentVersion);
            System.out.println("Latest version: " + latestVersion);
            System.out.println("Update notes: " + latestNotes);
            System.out.println("Please update to the newest version.");
            System.out.println("Download: https://www.spigotmc.org/resources/" + pluginName + ".75749/history");
            System.out.println("Donate: <donation link>");
            return true;
        }
    }//*/

	/**private String[] getLatestVersion(String versionType) {
        try {
            URL url = new URL(githubURL);
            InputStream stream = url.openStream();
            Scanner scanner = new Scanner(stream);
            scanner.useDelimiter("\\Z");
            String xmlContent = scanner.next();
            scanner.close();
            stream.close();

            String versionTag = "<" + versionType + ">";
            String notesTag = "<" + versionType + "-notes>";
            String endTag = "</" + versionType + ">";

            int versionStartIndex = xmlContent.indexOf(versionTag);
            int notesStartIndex = xmlContent.indexOf(notesTag);
            int versionEndIndex = xmlContent.indexOf(endTag);

            if (versionStartIndex == -1 || notesStartIndex == -1 || versionEndIndex == -1) {
                return null;
            }

            String versionNumber = xmlContent.substring(versionStartIndex + versionTag.length(), versionEndIndex);
            String notes = xmlContent.substring(notesStartIndex + notesTag.length(), xmlContent.indexOf(endTag + "-notes>"));
            String[] versionArray = new String[2];
            versionArray[0] = versionNumber;
            versionArray[1] = notes;
            return versionArray;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }//*/

	/**public static void main(String[] args) {
        //String pluginName = "MyPlugin";
        String currentVersion = "1.14_1.0.4";
        String githubURL = "https://github.com/JoelGodOfwar/NoEndermanGrief/raw/master/versions/1.14/versions.xml";
        VersionChecker checker = new VersionChecker(currentVersion, 71236, githubURL);
        checker.checkForUpdates();
    }//*/
}
