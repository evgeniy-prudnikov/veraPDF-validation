package org.verapdf.features.gf.objects;

import org.verapdf.core.FeatureParsingException;
import org.verapdf.cos.COSString;
import org.verapdf.features.*;
import org.verapdf.features.gf.tools.GFCreateNodeHelper;
import org.verapdf.features.tools.FeatureTreeNode;
import org.verapdf.pd.PDSignature;
import org.verapdf.tools.TypeConverter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Features object for digital signature.
 *
 * @author Sergey Shemyakov
 */
public class GFSigantureFeaturesObject implements IFeaturesObject {

    private PDSignature signature;

    public GFSigantureFeaturesObject(PDSignature signature) {
        this.signature = signature;
    }

    @Override
    public FeatureObjectType getType() {
        return FeatureObjectType.SIGNATURE;
    }

    @Override
    public FeatureTreeNode reportFeatures(FeatureExtractionResult featureExtractionResult) throws FeatureParsingException {
        if (signature != null) {
            FeatureTreeNode root = FeatureTreeNode.createRootNode("signature");

            GFCreateNodeHelper.addNotEmptyNode("filter", signature.getFilter(), root);
            GFCreateNodeHelper.addNotEmptyNode("subFilter", signature.getSubfilter(), root);
            GFCreateNodeHelper.addNotEmptyNode("contents", signature.getContents().getHexString(), root);
            GFCreateNodeHelper.addNotEmptyNode("name", signature.getName(), root);
            GFCreateNodeHelper.addNotEmptyNode("signDate", signature.getSignDate(), root);
            GFCreateNodeHelper.addNotEmptyNode("location", signature.getLocation(), root);
            GFCreateNodeHelper.addNotEmptyNode("reason", signature.getReason(), root);
            GFCreateNodeHelper.addNotEmptyNode("contactInfo", signature.getContactInfo(), root);

            featureExtractionResult.addNewFeatureTree(FeatureObjectType.SIGNATURE, root);

            return root;
        }
        return null;
    }

    @Override
    public FeaturesData getData() {
        COSString contents = signature.getContents();
        InputStream stream = contents == null ? null :
                new ByteArrayInputStream(contents.getHexString().getBytes());
        return SignatureFeaturesData.newInstance(
                stream, signature.getFilter().getValue(),
                signature.getSubfilter().getValue(), signature.getName(),
                TypeConverter.parseDate(signature.getSignDate()), signature.getLocation(),
                signature.getReason(), signature.getContactInfo());
    }
}
