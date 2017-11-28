/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.synopsys.protecode.sc.jenkins.types;

import com.synopsys.protecode.sc.jenkins.exceptions.MalformedSha1SumException;
import lombok.Data;

/**
 *
 * @author pajunen
 */
public class InternalTypes {
    public static @Data class Secret {
        private final String string;

        public String string() {
            return string;
        }

        @Override
        public String toString() {
            return "###########";
        }
    }
    
    /**
     * Must be 40 characters long
    * "sha1sum": "3fcdbdb04baa29ce695ff36af81eaac496364e82"
    */
    public static @Data class Sha1Sum {
        private final String sha1sum;

        public Sha1Sum(String sum) {
            // TODO: add regex for this.
            if (sum.length() == 40) {
                sha1sum = sum;
            } else {
                throw new MalformedSha1SumException("incorrect length of sha1sum, "
                    + "must be 40 characters long");
            }
        }

        public String toString() {
            return sha1sum;
        }
    }
    
    public static @Data class Group {
        private String name;
    }
}
