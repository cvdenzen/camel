/*
 * Salesforce Query DTO generated by camel-salesforce-maven-plugin
 */
package $packageName;

import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.apache.camel.component.salesforce.api.dto.AbstractQueryRecordsBase;

import java.util.List;
import javax.annotation.Generated;

/**
 * Salesforce QueryRecords DTO for type ComplexCalculatedFormula
 */
@Generated("org.apache.camel.maven.CamelSalesforceMojo")
public class QueryRecordsComplexCalculatedFormula extends AbstractQueryRecordsBase {

    @XStreamImplicit
    private List<ComplexCalculatedFormula> records;

    public List<ComplexCalculatedFormula> getRecords() {
        return records;
    }

    public void setRecords(List<ComplexCalculatedFormula> records) {
        this.records = records;
    }
}
