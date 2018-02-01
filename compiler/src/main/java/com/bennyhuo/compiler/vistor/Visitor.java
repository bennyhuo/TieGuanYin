package com.bennyhuo.compiler.vistor;

import com.bennyhuo.compiler.basic.OptionalField;
import com.bennyhuo.compiler.basic.RequiredField;

import javax.annotation.processing.Filer;

/**
 * Created by benny on 2/2/18.
 */

public interface Visitor {

    void visit(RequiredField field);

    void visit(OptionalField field);

    void visit(Filer filer);

}
