/**
 * This file is part of validation-model, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * validation-model is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with validation-model as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * validation-model as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.gf.model.impl.pd;

import org.verapdf.as.ASAtom;
import org.verapdf.cos.COSObjType;
import org.verapdf.cos.COSObject;
import org.verapdf.model.baselayer.Object;
import org.verapdf.pd.PDHalftone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Maksim Bezrukov
 */
public class GFPDHalftone extends GFPDObject implements org.verapdf.model.pdlayer.PDHalftone {

    public static final String HALFTONE_TYPE = "PDHalftone";

    private static final String HALFTONES = "halftones";

    private final ASAtom colorantName;

    public GFPDHalftone(PDHalftone dict) {
        this(dict, null);
    }

    public GFPDHalftone(PDHalftone dict, ASAtom colorantName) {
        super(dict, HALFTONE_TYPE);
        this.colorantName = colorantName;
    }

    @Override
    public Long getHalftoneType() {
        PDHalftone halftone = (PDHalftone) this.simplePDObject;
        COSObject object = halftone.getObject();
        if (object.getType() == COSObjType.COS_NAME && object.getName() == ASAtom.DEFAULT) {
            return Long.valueOf(1L);
        }
        return halftone.getHalftoneType();
    }

    @Override
    public String getHalftoneName() {
        PDHalftone halftone = (PDHalftone) this.simplePDObject;
        COSObject object = halftone.getObject();
        if (object.getType() == COSObjType.COS_NAME && object.getName() == ASAtom.DEFAULT) {
            return null;
        }
        return halftone.getHalftoneName();
    }

    @Override
    public String getcolorantName() {
        return this.colorantName == null ? null : this.colorantName.getValue();
    }

    @Override
    public String getTransferFunction() {
        COSObject tf = this.simplePDObject.getKey(ASAtom.getASAtom("TransferFunction"));
        if (tf == null
                || tf.empty()
                || tf.getType() == COSObjType.COS_NULL) {
            return null;
        }

        if (tf.getType() == COSObjType.COS_NAME) {
            return tf.getName().getValue();
        }
        return tf.toString();
    }

    @Override
    public List<? extends Object> getLinkedObjects(String link) {
        switch (link) {
            case HALFTONES:
                return this.getHalftones();
            default:
                return super.getLinkedObjects(link);
        }
    }

    private List<org.verapdf.model.pdlayer.PDHalftone> getHalftones() {
        Long halftoneType = getHalftoneType();
        if (halftoneType == null || halftoneType.longValue() != 5L) {
            return Collections.emptyList();
        }
        List<org.verapdf.model.pdlayer.PDHalftone> halftones = new ArrayList<>();
        COSObject object = this.simplePDObject.getObject();
        if (object != null && object.getType().isDictionaryBased()) {
            for (ASAtom key : object.getKeySet()) {
                COSObject value = object.getKey(key);
                if (value.getType().isDictionaryBased()) {
                    PDHalftone halftone = new PDHalftone(value);
                    GFPDHalftone gfPDHalftone = new GFPDHalftone(halftone, key);
                    halftones.add(gfPDHalftone);
                }
            }
        }
        return Collections.unmodifiableList(halftones);
    }
}
