package com.pw;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnmappableCharacterException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter path and filename of the source file: ");
        String path = scanner.nextLine();
        List<String> tracklist = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8).stream()
                .filter(Main::isTimecode)
                .collect(Collectors.toList());
        StringBuilder output = new StringBuilder();
        output.append("PERFORMER \"PERFORMER\"").append(System.lineSeparator())
                .append("TITLE \"TITLE\"").append(System.lineSeparator())
                .append("FILE \"FILE\" WAVE").append(System.lineSeparator());
        int trackNumber = 1;
        for (String line : tracklist) {
            String artistAndTitle = line.substring(line.indexOf(" ")).trim().replaceAll("[^0-9A-Za-z-– .,()'&]+", "");
            if (Character.isDigit(artistAndTitle.charAt(0))) {
                artistAndTitle = artistAndTitle.substring(artistAndTitle.indexOf(" "));
            }
            int splitIndex = artistAndTitle.indexOf(" - ");
            if (splitIndex <= 0) {
                splitIndex = artistAndTitle.indexOf("-");
            }
            if (splitIndex <= 0) {
                splitIndex = artistAndTitle.indexOf("–");
            }
            String artist;
            String title;
            if (splitIndex <= 0) {
                artist = "";
                title = artistAndTitle;
            } else {
                artist = artistAndTitle.substring(0, splitIndex).replaceAll("-", "").replaceAll("–", "").trim();
                title = artistAndTitle.substring(splitIndex).replaceAll("-", "").replaceAll("–", "").trim();
            }
            String trackNumberStr = trackNumber < 10 ? "0" + trackNumber : String.valueOf(trackNumber);
            String trackNumberLine = "  TRACK " + trackNumberStr + " AUDIO" + System.lineSeparator();
            output.append(trackNumberLine);
            String titleLine = "\tTITLE \"" + title + "\"" + System.lineSeparator();
            output.append(titleLine);
            String artistLine = "\tPERFORMER \"" + artist + "\"" + System.lineSeparator();
            output.append(artistLine);
            String[] timecode = line.substring(0, line.indexOf(" ")).replaceAll("[^0-9:]+", "").trim().split(":");
            boolean isWithHours = timecode.length > 2;
            String minutes;
            if (isWithHours) {
                minutes = String.valueOf(Integer.parseInt(timecode[0]) * 60 + Integer.parseInt(timecode[1]));
            } else {
                minutes = timecode[0];
            }
            if (minutes.length() == 1) {
                minutes = "0" + minutes;
            }
            String seconds = isWithHours ? timecode[2] : timecode[1];
            if (seconds.length() == 1) {
                seconds = "0" + seconds;
            }
            String timecodeLine = "\tINDEX 01 " + minutes + ":" + seconds + ":00" + System.lineSeparator();
            output.append(timecodeLine);
            trackNumber++;
        }
        try {
            String outputPath = Paths.get(path).getParent().toString() + "output.cue";
            Files.writeString(Paths.get(outputPath), output.toString(), StandardCharsets.UTF_8);
            System.out.println("Output file saved to " + outputPath);
        } catch (UnmappableCharacterException e) {
            e.printStackTrace();
        }
    }

    private static boolean isTimecode(String line) {
        if (line.contains(":") && line.contains(" ") && !line.contains(":/")) {
            String timecode = line.substring(0, line.indexOf(" ")).trim().replaceAll("[^0-9:]+", "");
            return timecode.indexOf(":") <= 2 && timecode.indexOf(":") > 0;
        } else {
            return false;
        }
    }
}
