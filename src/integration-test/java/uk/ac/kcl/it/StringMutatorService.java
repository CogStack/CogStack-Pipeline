/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.kcl.it;


import com.google.common.collect.ImmutableMap;
import de.svenjacobs.loremipsum.LoremIpsum;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author rich
 */
@Ignore
@Service("stringMutatorService")
@ComponentScan("uk.ac.kcl.it")
public class StringMutatorService {

    @Autowired
    private MagicSquare ms = null;

    @Value("#{'${humanMistypeMutationRate:8}'}")
    private int humanMistypeMutationRate;
    @Value("#{'${badOCRMutationRate:8}'}")
    private int badOCRMutationRate;
    @Value("#{'${loremLength:10}'}")
    private int loremLength;

    private ImmutableMap<Integer, Character> characterMap;
    private Map<Character, Integer> charTotals;
    private HashMap<Character, Integer> characterMapKeys;
    private final Random random = new Random();
    @Value("#{'${minAddressTokenCount:3}'}")
    private int minAddressTokenCount;
    @Value("#{'${probOfAddressTokenTruncation:50}'}")
    private int probOfAddressTokenTruncation;

    public ImmutableMap<String, String> addressAbbrevMap;


    @PostConstruct
    private void init() {
        this.characterMap = ImmutableMap.<Integer, Character>builder()
                .put(0, "A".charAt(0)).put(1, "B".charAt(0)).put(2, "C".charAt(0)).put(3, "D".charAt(0)).put(4, "E"
                        .charAt(0)).put(5, "F".charAt(0)).put(6, "G".charAt(0)).put(7, "H".charAt(0)).put(8, "I"
                        .charAt(0)).put(9, "J".charAt(0)).put(10, "K".charAt(0)).put(11, "L".charAt(0)).put(12, "M"
                        .charAt(0)).put(13, "N".charAt(0)).put(14, "O".charAt(0)).put(15, "P".charAt(0)).put(16, "Q"
                        .charAt(0)).put(17, "R".charAt(0)).put(18, "S".charAt(0)).put(19, "T".charAt(0)).put(20, "U"
                        .charAt(0)).put(21, "V".charAt(0)).put(22, "W".charAt(0)).put(23, "X".charAt(0)).put(24, "Y"
                        .charAt(0)).put(25, "Z".charAt(0)).build();
        this.characterMapKeys = new HashMap<>();
        for (Map.Entry<Integer, Character> entry : characterMap.entrySet()) {
            characterMapKeys.put(entry.getValue(), entry.getKey());
        }

        this.addressAbbrevMap = ImmutableMap.<String, String>builder()
                .put("ALY", "ALLEY").put("ANX ", "ANNEX").put("APT ", "APARTMENT").put("ARC ", "ARCADE").put("AVE ",
                        "AVENUE").put("BSMT ", "BASEMENT").put("BYU ", "BAYOU").put("BCH ", "BEACH").put("BND ",
                        "BEND").put("BLF ", "BLUFF").put("BTM ", "BOTTOM").put("BLVD ", "BOULEVARD").put("BR ",
                        "BRANCH").put("BRG ", "BRIDGE").put("BRK ", "BROOK").put("BLDG ", "BUILDING").put("BG ",
                        "BURG").put("BYP ", "BYPASS").put("CP ", "CAMP").put("CYN ", "CANYON").put("CPE ", "CAPE")
                .put("CSWY ", "CAUSEWAY").put("CTR ", "CENTER").put("CIR ", "CIRCLE").put("CLF ", "CLIFF").put("CLFS" +
                        " ", "CLIFFS").put("CLB ", "CLUB").put("COR ", "CORNER").put("CORS ", "CORNERS").put("CRSE ",
                        "COURSE").put("CT ", "COURT").put("CTS ", "COURTS").put("CV ", "COVE").put("CRK ", "CREEK")
                .put("CRES ", "CRESCENT").put("XING ", "CROSSING").put("DL ", "DALE").put("DM ", "DAM").put("DEPT ",
                        "DEPARTMENT").put("DV ", "DIVIDE").put("DR ", "DRIVE").put("EST ", "ESTATE").put("EXPY ",
                        "EXPRESSWAY").put("EXT ", "EXTENSION").put("FLS ", "FALLS").put("FRY ", "FERRY").put("FLD ",
                        "FIELD").put("FLDS ", "FIELDS").put("FLT ", "FLAT").put("FL ", "FLOOR").put("FRD ", "FORD")
                .put("FRST ", "FOREST").put("FRG ", "FORGE").put("FRK ", "FORK").put("FRKS ", "FORKS").put("FT ",
                        "FORT").put("FWY ", "FREEWAY").put("FRNT ", "FRONT").put("GDN ", "GARDEN").put("GDNS ",
                        "GARDENS").put("GTWY ", "GATEWAY").put("GLN ", "GLEN").put("GRN ", "GREEN").put("GRV ",
                        "GROVE").put("HNGR ", "HANGER").put("HBR ", "HARBOR").put("HVN ", "HAVEN").put("HTS ",
                        "HEIGHTS").put("HWY ", "HIGHWAY").put("HL ", "HILL").put("HLS ", "HILLS").put("HOLW ",
                        "HOLLOW").put("INLT ", "INLET").put("IS ", "ISLAND").put("ISS ", "ISLANDS").put("JCT ",
                        "JUNCTION").put("KY ", "KEY").put("KNL ", "KNOLL").put("KNLS ", "KNOLLS").put("LK ", "LAKE")
                .put("LKS ", "LAKES").put("LNDG ", "LANDING").put("LN ", "LANE").put("LGT ", "LIGHT").put("LF ",
                        "LOAF").put("LBBY ", "LOBBY").put("LCK ", "LOCK").put("LCKS ", "LOCKS").put("LDG ", "LODGE")
                .put("LOWR ", "LOWER").put("MNR ", "MANOR").put("MDW ", "MEADOW").put("MDWS ", "MEADOWS").put("ML ",
                        "MILL").put("MLS ", "MILLS").put("MSN ", "MISSION").put("MT ", "MOUNT").put("MTN ",
                        "MOUNTAIN").put("NCK ", "NECK").put("OFC ", "OFFICE").put("ORCH ", "ORCHARD").put("PKWY ",
                        "PARKWAY").put("PH ", "PENTHOUSE").put("PNE ", "PINE").put("PNES ", "PINES").put("PL ",
                        "PLACE").put("PLN ", "PLAIN").put("PLNS ", "PLAINS").put("PLZ ", "PLAZA").put("PT ", "POINT")
                .put("PRT ", "PORT").put("PR ", "PRAIRIE").put("RADL ", "RADIAL").put("RNCH ", "RANCH").put("RPD ",
                        "RAPID").put("RPDS ", "RAPIDS").put("RST ", "REST").put("RDG ", "RIDGE").put("RIV ", "RIVER")
                .put("RD ", "ROAD").put("RM ", "ROOM").put("SHL ", "SHOAL").put("SHLS ", "SHOALS").put("SHR ",
                        "SHORE").put("SHRS ", "SHORES").put("SPC ", "SPACE").put("SPG ", "SPRING").put("SPGS ",
                        "SPRINGS").put("SQ ", "SQUARE").put("STA ", "STATION").put("STRA ", "STRAVENUE").put("STRM ",
                        "STREAM").put("ST ", "STREET").put("STE ", "SUITE").put("SMT ", "SUMMIT").put("TER ",
                        "TERRACE").put("TRCE ", "TRACE").put("TRAK ", "TRACK").put("TRFY ", "TRAFFICWAY").put("TRL ",
                        "TRAIL").put("TRLR ", "TRAILER").put("TUNL ", "TUNNEL").put("TPKE ", "TURNPIKE").put("UN ",
                        "UNION").put("UPPR ", "UPPER").put("VLY ", "VALLEY").put("VIA ", "VIADUCT").put("VW ",
                        "VIEW").put("VLG ", "VILLAGE").put("VL ", "VILLE").put("VIS ", "VISTA")
                .put("WL ", "WELL").put("WLS ", "WELLS").put("ALLEY ", "ALY").put("ANNEX ", "ANX").put("APARTMENT ",
                        "APT").put("ARCADE ", "ARC").put("AVENUE ", "AVE").put("BASEMENT ", "BSMT").put("BAYOU ",
                        "BYU").put("BEACH ", "BCH").put("BEND ", "BND").put("BLUFF ", "BLF").put("BOTTOM ", "BTM")
                .put("BOULEVARD ", "BLVD").put("BRANCH ", "BR").put("BRIDGE ", "BRG").put("BROOK ", "BRK").put
                        ("BUILDING ", "BLDG").put("BURG ", "BG").put("BYPASS ", "BYP").put("CAMP ", "CP").put("CANYON" +
                        " ", "CYN").put("CAPE ", "CPE").put("CAUSEWAY ", "CSWY").put("CENTER ", "CTR").put("CIRCLE ",
                        "CIR").put("CLIFF ", "CLF").put("CLIFFS ", "CLFS").put("CLUB ", "CLB").put("CORNER ", "COR")
                .put("CORNERS ", "CORS").put("COURSE ", "CRSE").put("COURT ", "CT").put("COURTS ", "CTS").put("COVE " +
                        "", "CV").put("CREEK ", "CRK").put("CRESCENT ", "CRES").put("CROSSING ", "XING").put("DALE ",
                        "DL").put("DAM ", "DM").put("DEPARTMENT ", "DEPT").put("DIVIDE ", "DV").put("DRIVE ", "DR")
                .put("ESTATE ", "EST").put("EXPRESSWAY ", "EXPY").put("EXTENSION ", "EXT").put("FALLS ", "FLS").put
                        ("FERRY ", "FRY").put("FIELD ", "FLD").put("FIELDS ", "FLDS").put("FLAT ", "FLT").put("FLOOR " +
                        "", "FL").put("FORD ", "FRD").put("FOREST ", "FRST").put("FORGE ", "FRG").put("FORK ", "FRK")
                .put("FORKS ", "FRKS").put("FORT ", "FT").put("FREEWAY ", "FWY").put("FRONT ", "FRNT").put("GARDEN ",
                        "GDN").put("GARDENS ", "GDNS").put("GATEWAY ", "GTWY").put("GLEN ", "GLN").put("GREEN ",
                        "GRN").put("GROVE ", "GRV").put("HANGER ", "HNGR").put("HARBOR ", "HBR").put("HAVEN ", "HVN")
                .put("HEIGHTS ", "HTS").put("HIGHWAY ", "HWY").put("HILL ", "HL").put("HILLS ", "HLS").put("HOLLOW ",
                        "HOLW").put("INLET ", "INLT").put("ISLAND ", "IS").put("ISLANDS ", "ISS").put("JUNCTION ",
                        "JCT").put("KEY ", "KY").put("KNOLL ", "KNL").put("KNOLLS ", "KNLS").put("LAKE ", "LK").put
                        ("LAKES ", "LKS").put("LANDING ", "LNDG").put("LANE ", "LN").put("LIGHT ", "LGT").put("LOAF " +
                        "", "LF").put("LOBBY ", "LBBY").put("LOCK ", "LCK").put("LOCKS ", "LCKS").put("LODGE ",
                        "LDG").put("LOWER ", "LOWR").put("MANOR ", "MNR").put("MEADOW ", "MDW").put("MEADOWS ",
                        "MDWS").put("MILL ", "ML").put("MILLS ", "MLS").put("MISSION ", "MSN").put("MOUNT ", "MT")
                .put("MOUNTAIN ", "MTN").put("NECK ", "NCK").put("OFFICE ", "OFC").put("ORCHARD ", "ORCH").put
                        ("PARKWAY ", "PKWY").put("PENTHOUSE ", "PH").put("PINE ", "PNE").put("PINES ", "PNES").put
                        ("PLACE ", "PL").put("PLAIN ", "PLN").put("PLAINS ", "PLNS").put("PLAZA ", "PLZ").put("POINT " +
                        "", "PT").put("PORT ", "PRT").put("PRAIRIE ", "PR").put("RADIAL ", "RADL").put("RANCH ",
                        "RNCH").put("RAPID ", "RPD").put("RAPIDS ", "RPDS").put("REST ", "RST").put("RIDGE ", "RDG")
                .put("RIVER ", "RIV").put("ROAD ", "RD").put("ROOM ", "RM").put("SHOAL ", "SHL").put("SHOALS ",
                        "SHLS").put("SHORE ", "SHR").put("SHORES ", "SHRS").put("SPACE ", "SPC").put("SPRING ",
                        "SPG").put("SPRINGS ", "SPGS").put("SQUARE ", "SQ").put("STATION ", "STA").put("STRAVENUE ",
                        "STRA").put("STREAM ", "STRM").put("STREET ", "ST").put("SUITE ", "STE").put("SUMMIT ",
                        "SMT").put("TERRACE ", "TER").put("TRACE ", "TRCE").put("TRACK ", "TRAK").put("TRAFFICWAY ",
                        "TRFY").put("TRAIL ", "TRL").put("TRAILER ", "TRLR").put("TUNNEL ", "TUNL").put("TURNPIKE ",
                        "TPKE").put("UNION ", "UN").put("UPPER ", "UPPR").put("VALLEY ", "VLY").put("VIADUCT ",
                        "VIA").put("VIEW ", "VW").put("VILLAGE ", "VLG").put("VILLE ", "VL").put("VISTA ", "VIS")
                .put("WELL ", "WL").put("WELLS", "WLS").build();
    }

    public int getLoremLength() {
        return loremLength;
    }

    public void setLoremLength(int loremLength) {
        this.loremLength = loremLength;
    }

    public int getBadOCRMutationRate() {
        return badOCRMutationRate;
    }

    public void setBadOCRMutationRate(int badOCRMutationRate) {
        this.badOCRMutationRate = badOCRMutationRate;
    }

    public int getMinAddressTokenCount() {
        return minAddressTokenCount;
    }

    public void setMinAddressTokenCount(int minAddressTokenCount) {
        this.minAddressTokenCount = minAddressTokenCount;
    }


    public int getProbOfAddressTokenTruncation() {
        return probOfAddressTokenTruncation;
    }

    public void setProbOfAddressTokenTruncation(int probOfAddressTokenTruncation) {
        this.probOfAddressTokenTruncation = probOfAddressTokenTruncation;
    }

    public void calcCharTotals() {
        charTotals = new HashMap<>();
        int[][] matrix = ms.getMatrix();
        for (int i = 0; i < matrix.length; i++) {
            int total = 0;
            for (int j = 0; j < matrix[i].length; j++) {
                total = total + matrix[i][j];
            }
            charTotals.put(characterMap.get(i), total);
        }
    }

    public String getMutation(char c, int hit) {

        String returnC = "";
        int i = random.nextInt(3);
        int[][] matrix = ms.getMatrix();
        int cursor = 0;
        int j;
        for (j = 0; j < matrix[characterMapKeys.get(c)].length; j++) {
            cursor = cursor + matrix[characterMapKeys.get(c)][j];
            if (cursor >= hit) {
                break;
            }
        }
        if (i == 0) {
            returnC = "";

        } else if (i == 1) {

        } else {
            returnC = c + characterMap.get(j).toString();
        }
        return returnC;
    }


    public void setHumanMistypeMutationRate(int humanMistypeMutationRate) {
        this.humanMistypeMutationRate = humanMistypeMutationRate;
    }


    public Map<Character, Integer> getCharTotals() {
        return charTotals;
    }

    public void setCharTotals(Map<Character, Integer> charTotals) {
        this.charTotals = charTotals;
    }

    public HashMap<Character, Integer> getCharacterMapKeys() {
        return characterMapKeys;
    }

    public void setCharacterMapKeys(HashMap<Character, Integer> characterMapKeys) {
        this.characterMapKeys = characterMapKeys;
    }

    public String generateMutantDocument(String name, String address, String postcode, String date) {

        LoremIpsum loremIpsum = new LoremIpsum();

        StringBuilder sb = new StringBuilder();
        sb.insert(0, loremIpsum.getWords(random.nextInt(loremLength)));
        if (name != null) sb.append(" ").append(mutate(name));
        sb.append(" ").append(loremIpsum.getWords(random.nextInt(loremLength))).append(" ");
        if (address != null) sb.append(" ").append(mutate(address));
        sb.append(" ").append(loremIpsum.getWords(random.nextInt(loremLength))).append(" ");
        if (postcode != null) sb.append(" ").append(mutate(postcode));
        sb.append(" ").append(loremIpsum.getWords(random.nextInt(loremLength)));
        if (date != null) sb.append(" ").append(date);
        sb.append(" ").append(loremIpsum.getWords(random.nextInt(loremLength)));
        return sb.toString();
    }

    public String mutate(String normal) {
        int i = random.nextInt(4);
        if (i == 0) {
            normal = subCharacters(normal, humanMistypeMutationRate);
        } else if (i == 1) {
            normal = generateAliases(normal);
        } else if (i == 2) {
            normal = removeAddressTokens(normal);
        } else if (i == 3) {
            normal = simulateBadOCR(normal);
        }


        return normal;
    }

    private String generateAliases(String normal) {
        if (random.nextInt(2) == 1) {
            for (Map.Entry<String, String> entry : addressAbbrevMap.entrySet()) {

                normal = normal.replaceAll("(?)^" + entry.getKey() + "$", entry.getValue());
            }
        }
        return normal;
    }

    private String subCharacters(String normal, int mutationRate) {
        char[] array = normal.toCharArray();
        calcCharTotals();
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < array.length; i++) {
            if (random.nextInt(100) <= mutationRate) {
                if (characterMapKeys.containsKey(array[i])) {
                    char a = array[i];
                    int b = charTotals.get(array[i]);
                    int c = random.nextInt(b);
                    //array[i] = getMutation(a,c);
                    sb.append(getMutation(a, c));
                }
            } else {
                sb.append(array[i]);
            }
        }
        return sb.toString();
    }

    private String removeAddressTokens(String normal) {
        String mutant = "";
        StringTokenizer st = new StringTokenizer(normal);
        int totalCount = st.countTokens();
        for (int i = 0; i < totalCount; i++) {
            if (i <= minAddressTokenCount) {
                mutant = mutant + " " + st.nextToken();
            } else if (i > minAddressTokenCount && random.nextInt(100) <= probOfAddressTokenTruncation) {
                mutant = mutant + " " + st.nextToken();
            } else {
                break;
            }
        }
        return mutant;
    }

    private String simulateBadOCR(String normal) {
        normal = subCharacters(normal, badOCRMutationRate);
        char[] array = normal.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]).append(" ");
        }
        return sb.toString();
    }
}