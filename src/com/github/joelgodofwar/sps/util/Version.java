package com.github.joelgodofwar.sps.util;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;


public class Version implements Comparable<Version>, Serializable {
	/**
	 * 
	 */
	/**
	 * The latest release version of minecraft.
	 */
	public static final Version LATEST = new Version("1.20.4");
	/**
	 * The minimum version Plugin has been tested with.
	 */
	public static final String MINIMUM_MINECRAFT_VERSION = "1.14";
	/**
	 * The maximum version Plugin has been tested with.
	 */
	public static final String MAXIMUM_MINECRAFT_VERSION = "1.20.5";
	public static final Version MC_1_14 = new Version("1.14");

	private static final long serialVersionUID = 6815052987222483030L;
	/**
	 * Regular expression used to parse version strings.
	 */
	//private static final Pattern VERSION_PATTERN = Pattern.compile(".*\\(.*MC.\\s*([a-zA-z0-9\\-.]+).*");

	/**
	 * The current version of minecraft, lazy initialized by MinecraftVersion.currentVersion()
	 */
	private static Version currentVersion;
	private int major; // 1
	private int minor; // 16
	private int patch; // 1
	private int build; // ?
	private volatile Boolean atCurrentOrAbove;

	/**
	 * Determine the current Minecraft version.
	 *
	 * @param server - the Bukkit server that will be used to examine the MC version.
	 */
	public Version(Server server) {
		this(extractVersion(server.getVersion()));
	}

	private String[] string2 = {"0","0","0","0"};
	public Version(String string){
		string2 = string.split("\\.");
		this.major = NumberUtils.toInt(string2[0]);
		this.minor = NumberUtils.toInt(string2[1]);
		this.patch = string2.length > 2 ? NumberUtils.toInt(string2[2]) : 0;
		this.build = string2.length >= 4 ? NumberUtils.toInt(string2[3]) : 0;
	}

	/**
	 * Construct a version object directly.
	 *
	 * @param major - major version number.
	 * @param minor - minor version number.
	 * @param patch - patch version number.
	 */
	public Version(int major, int minor, int patch) {
		this(major, minor, patch, 0);
	}

	/**
	 * Construct a version object directly.
	 *
	 * @param major - major version number.
	 * @param minor - minor version number.
	 * @param patch - patch version number.
	 * @param build - build version number.
	 */
	public Version(int major, int minor, int patch, int build) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.build = build;
	}

	/**
	 * Extract the Minecraft version from CraftBukkit itself.
	 *
	 * @param text - the server version in text form.
	 * @return The underlying MC version.
	 * @throws IllegalStateException If we could not parse the version string.
	 */
	public static String extractVersion(String strVersion) {
		//String strVersion = Bukkit.getVersion().toString();
		strVersion = strVersion.substring(strVersion.indexOf("MC: "), strVersion.length());
		strVersion = strVersion.replace("MC: ", "").replace(")", "");
		return strVersion;
	}

	/**
	 * Parse the given server version into a Minecraft version.
	 *
	 * @param serverVersion - the server version.
	 * @return The resulting Minecraft version.
	 */
	public static Version fromServerVersion(String serverVersion) {
		return new Version(extractVersion(serverVersion));
	}

	public static Version getCurrentVersion() {
		if (currentVersion == null) {
			currentVersion = fromServerVersion(Bukkit.getVersion());
		}

		return currentVersion;
	}

	public static void setCurrentVersion(Version version) {
		currentVersion = version;
	}

	public boolean atOrAbove(Version version) {
		return getCurrentVersion().isAtLeast(version);
	}

	public int[] parseVersion(String version) {
		String[] elements = version.split("\\.");
		int[] numbers = new int[3];

		// Make sure it's even a valid version
		if (elements.length < 1) {
			throw new IllegalStateException("Corrupt MC version: " + version);
		}

		// The String 1 or 1.2 is interpreted as 1.0.0 and 1.2.0 respectively.
		for (int i = 0; i < Math.min(numbers.length, elements.length); i++) {
			numbers[i] = Integer.parseInt(elements[i].trim());
		}
		return numbers;
	}

	public int getMajor(){return major;}
	public int getMinor(){return minor;}
	public int getPatch(){return patch;}
	public int getBuild(){return build;}

	/**
	 * Checks if this version is at or above the current version the server is running.
	 *
	 * @return true if this version is equal or newer than the server version, false otherwise.
	 */
	public boolean atOrAbove() {
		if (this.atCurrentOrAbove == null) {
			this.atCurrentOrAbove = atOrAbove(this);
		}

		return this.atCurrentOrAbove;
	}
	/**
	 * Retrieve the version String (major.minor.build) only.
	 *
	 * @return A normal version string.
	 */
	public String getVersion() {
		if (this.getBuild() == 0) {
			return String.format("%s.%s.%s", this.getMajor(), this.getMinor(), this.getBuild());
		} else {
			return String.format("%s.%s.%s-%s", this.getMajor(), this.getMinor(), this.getBuild(),
					this.getBuild());
		}
	}
	@Override
	public int compareTo(Version o) {
		if (o == null) {
			return 1;
		}
		return ComparisonChain.start()
				.compare(this.getMajor(), o.getMajor())
				.compare(this.getMinor(), o.getMinor())
				.compare(this.getPatch(), o.getPatch())
				.compare(this.getBuild(), o.getBuild(), Ordering.natural().nullsLast())
				.result();
	}
	public boolean isAtLeast(Version other) {
		if (other == null) {
			return false;
		}
		return this.compareTo(other) >= 0;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		Version other;
		if (obj instanceof Version) {
			other = (Version) obj;
		}else if (obj instanceof String) {
			other = new Version((String) obj);
			return (this.getMajor() == other.getMajor()) &&
					(this.getMinor() == other.getMinor()) &&
					(this.getPatch() == other.getPatch()) &&
					Objects.equals(this.getBuild(), other.getBuild());
		}
		return false;
	}
	@Override
	public int hashCode() {
		return Objects.hash(this.getMajor(), this.getMinor(), this.getPatch());
	}
	@Override
	public String toString() {
		// Convert to a String that we can parse back again
		return String.format("%s", this.getVersion());
	}
}
