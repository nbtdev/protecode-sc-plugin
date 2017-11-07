package com.synopsys.protecode.sc.jenkins.types;

import com.google.gson.annotations.SerializedName;
import com.synopsys.protecode.sc.jenkins.exceptions.ApiException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.*;

public final class Types {
    
    // Don't instantiate this
    private Types(){
    }       
    
    public static @Data class ScanId {        
        private final int id;                                    
    }

    public static @Data class Meta {
        private final int code;  
    }
    
    public static @Data class Groups {
        private final Meta meta;
        private final Product[] products;                       
    }
    
    public static @Data class Product {
        private final int id;
        private final String name;
        private final int product_id;
        private final Object custom_data;
        private final String sha1sum;
        private final String status;
    }
    
    public static @Data class InfoLeak {       
        private final Meta meta;
    }

    public static @Data class Result {
        private Integer id;
        private String sha1sum;
        private Summary summary;
        private Collection<Component> components;
        private Status status;
        private String report_url;
        private Details details;
    }    

    
    public static @Data class Summary {
        private Verdict verdict;    
        @SerializedName("vuln-count")
        private VulnCount vulnCount;
    }

    public static @Data class Verdict {
        private String detailed;
        @SerializedName("short")
        private String shortDesc;
    }

    public static @Data class VulnCount {
        private Long total;
        private Long exact;
        private Long historical;        
    }

    public static @Data class Component {
        private License license;
        private Collection<String> tags;
        private Collection<Vulns> vulns;
        private String version;
        private String lib;        
        @SerializedName("vuln-count")
        private VulnCount vulnCount;        
        @SerializedName("custom_version")
        private String customVersion;
        private String subcomponent;       
    }

    public static @Data class Vulns {
        private boolean exact;
        private Vuln vuln;        
    }

    public static @Data class Vuln {
        private String cve;
        private String cvss;
        private String summary;
    }

    public static @Data class License {
        private String url;
        private String type;
        private String name;
    }

    public static @Data class Details {
        private Map<String, Integer> filetypes;
        private Map<String, List<String>> flagged;
    }

    public static @Data class Filetype {
        Map<String, Integer> val;
    }

    public static @Data class Flagged {
        Map<String, List<String>> val;
    }   
    
    public static @Data class Status {        
        private String value;    
        private List<String> validValues = Arrays.asList("A", "B", "C");
        
        public Status(String state) {
            if (validValues.contains(state)) {
                this.value = state;
            }
            else {
                throw new ApiException("Incorrect value given as state");
            }
        }        
        
        @Override
        public String toString() {
            switch (value) {
                case "B": return "Busy";
                case "R": return "Ready";
                case "F": return "Fail";
                default: throw new ApiException("Incorrect value exists as state");
            }            
        }
    }
}
