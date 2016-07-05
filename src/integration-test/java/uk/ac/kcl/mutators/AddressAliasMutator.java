package uk.ac.kcl.mutators;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by rich on 05/07/16.
 */
@Service
public class AddressAliasMutator implements Mutator {


    private ImmutableMap<String, String> addressAbbrevMap;
    @Value("#{'${replaceAliasesRate:75}'}")
    private int replaceAliasesRate;

    @PostConstruct
    private void init() {


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


    private Mutant replaceWithAliases(String normal) {
        StringTokenizer st = new StringTokenizer(normal);
        Mutant mutant = new Mutant();
        StringBuilder documentSB = new StringBuilder();

        while(st.hasMoreTokens()){
            String newToken = "";
            String token = st.nextToken();
            mutant.getInputTokens().add(token);
            if (random.nextInt(100) <= replaceAliasesRate) {
                for (Map.Entry<String, String> entry : addressAbbrevMap.entrySet()) {
                    newToken = token.replaceAll("(?)^" + entry.getKey() + "$", entry.getValue());
                }
            }
            mutant.getOutputTokens().add(newToken);
            documentSB.append(" ").append(newToken);
        }

        mutant.setFinalText(documentSB.toString().trim());
        return mutant;
    }



    @Override
    public Mutant mutate(String document) {
        return replaceWithAliases(document);
    }
}
