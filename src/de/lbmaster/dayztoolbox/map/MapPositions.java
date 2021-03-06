package de.lbmaster.dayztoolbox.map;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import de.lbmaster.dayztoolbox.utils.ByteUtils;

public class MapPositions extends MapObject {
	
	public static final String MAP_POSITIONS_NAME_PLAYER_SPAWNPOINTS = "player_positions";

	private List<MapPosition> positions = new ArrayList<MapPosition>();
	private String name;

	public MapPositions() {
		super(MapObjectType.MAP_POINTS);
	}

	public MapPositions(byte[] bytes) {
		this();
		loadFromBytes(bytes);
	}
	
	public List<MapPosition> getPositions() {
		return positions;
	}
	
	public Color getColor() {
		switch (name) {
		case "???":
			return Color.PINK;
		case "player_positions":
			return Color.BLUE.brighter();
		case "VehicleCivilianSedan":
		case "VehicleOffroadHatchback":
		case "VehicleTransitBus":
		case "VehicleV3SCargo":
		case "VehicleV3SChassis":
			return Color.WHITE;
		case "StaticHeliCrash":
			return Color.MAGENTA;
		case "StaticPoliceCar":
			return Color.YELLOW;
		default:
			return Color.PINK;
		}
	}
	
	public String getDisplayName() {
		if (name == null)
			return "???";
		switch (name) {
		case "player_positions":
			return "Player SpawnPoints";
		case "VehicleCivilianSedan":
			return "Civilian Sedan";
		case "StaticHeliCrash":
			return "Heli Crashsites";
		case "VehicleOffroadHatchback":
			return "Offroad Hatchback";
		case "StaticPoliceCar":
			return "Police Cars";
		case "VehicleTransitBus":
			return "Transit Bus";
		case "VehicleV3SCargo":
			return "V3S Cargo";
		case "VehicleV3SChassis":
			return "V3S Chassis";
		default:
			return name;
		}
	}

	public void loadFromPlayerPositionsFile(File file) throws IOException {
		if (file == null || !file.exists())
			throw new IllegalArgumentException("File is == null or not exsist !");
		this.name = MAP_POSITIONS_NAME_PLAYER_SPAWNPOINTS;
		String content = new String(Files.readAllBytes(file.toPath()));
		Document xml = Jsoup.parse(content);
		Element playerspawnpoints = xml.getElementsByTag("playerspawnpoints").get(0);
		Element generator_posbubbles = playerspawnpoints.getElementsByTag("generator_posbubbles").get(0);
		loadXZFromXML(generator_posbubbles);
	}
	
	public static List<MapPositions> loadEventPositions(File file) throws IOException {
		if (file == null || !file.exists())
			throw new IllegalArgumentException("File is == null or not exsist !");
		String content = new String(Files.readAllBytes(file.toPath()));
		Document xml = Jsoup.parse(content);
		Element eventposdef = xml.getElementsByTag("eventposdef").get(0);
		List<MapPositions> positions = new ArrayList<MapPositions>();
		for (Element e : eventposdef.getElementsByTag("event")) {
			MapPositions pos = new MapPositions();
			pos.setName(e.attr("name"));
			pos.loadXZFromXML(e);
			System.out.println("Added Position: " + pos.getName() + " Locations: " + pos.getPositionsCount());
			positions.add(pos);
		}
		return positions;
	}
	
	private void loadXZFromXML(Element rootElement) {
		for (Element e : rootElement.getAllElements()) {
			if (e.tagName().equals("pos"))
				positions.add(new MapPosition(Double.parseDouble(e.attr("x")), Double.parseDouble(e.attr("z"))));
		}
	}

	public void loadFromBytes(byte[] bytes) {
		int size = ByteUtils.readInt(bytes, 0);
		System.out.println("Map Positions Size: " + size);
		int pos = 4;
		for (int i = 0; i < size; i++) {
			byte length = bytes[pos];
			pos += 1;
			byte[] data = ByteUtils.substring(bytes, pos, length);
			positions.add(new MapPosition(data));
			pos += length;
		}
		if (bytes.length > pos+1) {
			byte nameLength = bytes[pos];
			this.name = new String(ByteUtils.substring(bytes, pos+1, nameLength));
		}
	}

	public int getPositionsCount() {
		return positions.size();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public MapPosition getPositionByIndex(int index) {
		return positions.get(index);
	}

	public void addPosition(MapPosition pos) {
		positions.add(pos);
	}
	
	public void removePosition(String pos) {
		MapPosition pos2 = findPosByString(pos);
		if (pos2 != null)
			positions.remove(pos2);
	}
	
	private MapPosition findPosByString(String toString) {
		for (MapPosition pos : positions) {
			if (pos.toString().equals(toString))
				return pos;
		}
		return null;
	}

	@Override
	public byte[] toBytes() throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ByteArrayOutputStream content = new ByteArrayOutputStream();
		for (MapPosition pos : positions) {
			byte[] posBytes = pos.toBytes();
			bytes.write(new byte[] { (byte) (posBytes.length) });
			bytes.write(posBytes);
		}
		content.write(ByteUtils.intToBytes(positions.size()));
		content.write(bytes.toByteArray());
		if (this.name != null) {
			content.write(new byte[] {(byte) name.length()});
			content.write(name.getBytes());
		}
		return content.toByteArray();
	}

}