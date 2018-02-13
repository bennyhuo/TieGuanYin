package com.bennyhuo.tieguanyin.compiler.shared;

import com.bennyhuo.tieguanyin.annotations.SharedElement;
import com.bennyhuo.tieguanyin.annotations.SharedElementByNames;
import com.bennyhuo.tieguanyin.annotations.SharedElementWithName;
import com.squareup.kotlinpoet.FunSpec;

/**
 * Created by benny on 2/13/18.
 */

public class SharedElementEntity {
    public final String targetName;
    public final int sourceId;
    public final String sourceName;

    public SharedElementEntity(SharedElement sharedElement) {
        this(sharedElement.transitionName(), sharedElement.viewId());
    }

    public SharedElementEntity(SharedElementWithName sharedElement) {
        this(sharedElement.value(), sharedElement.value());
    }

    public SharedElementEntity(SharedElementByNames sharedElement) {
        this(sharedElement.target(), sharedElement.source());
    }

    public SharedElementEntity(String targetName, int sourceId) {
        this.targetName = targetName;
        this.sourceId = sourceId;
        sourceName = null;
    }

    public SharedElementEntity(String targetName, String sourceName) {
        this.targetName = targetName;
        this.sourceName = sourceName;
        this.sourceId = 0;
    }

    public void addStatementKt(FunSpec.Builder funBuilder){
        if(sourceId == 0){

        } else {
            funBuilder.addStatement("sharedElements.add(Pair(container.findViewById(%L), %S))", sourceId, targetName);
        }
    }
}
