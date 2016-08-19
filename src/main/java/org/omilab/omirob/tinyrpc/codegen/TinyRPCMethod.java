package org.omilab.omirob.tinyrpc.codegen;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Martin on 22.07.2016.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TinyRPCMethod {
        byte   id();
}
