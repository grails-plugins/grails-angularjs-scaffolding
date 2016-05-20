package grails.plugin.angular.scaffolding.model.property

import grails.core.GrailsDomainClass
import grails.util.GrailsNameUtils
import grails.validation.Constrained
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.validation.GrailsDomainClassValidator

import static grails.validation.ConstrainedProperty.BLANK_CONSTRAINT


class DomainPropertyImpl implements DomainProperty {

    @Delegate PersistentProperty property
    PersistentProperty rootProperty
    protected GrailsDomainClass grailsDomainClass
    PersistentEntity domainClass
    Constrained constraints
    String pathFromRoot

    protected Boolean convertEmptyStringsToNull
    protected Boolean trimStrings

    DomainPropertyImpl(PersistentProperty persistentProperty, MappingContext mappingContext) {
        this.property = persistentProperty
        this.domainClass = persistentProperty.owner
        this.grailsDomainClass = ((GrailsDomainClassValidator) mappingContext.getEntityValidator(domainClass)).domainClass
        this.constraints = (Constrained)grailsDomainClass.constrainedProperties[name]
        this.pathFromRoot = persistentProperty.name
    }

    DomainPropertyImpl(PersistentProperty rootProperty, PersistentProperty persistentProperty, MappingContext mappingContext) {
        this(persistentProperty, mappingContext)
        this.rootProperty = rootProperty
    }

    void setRootProperty(PersistentProperty rootProperty) {
        this.rootProperty = rootProperty
        this.pathFromRoot = "${rootProperty.name}.${property.name}"
    }

    Class getRootBeanType() {
        (rootProperty ?: property).owner.javaClass
    }

    Class getBeanType() {
        property.owner.javaClass
    }

    boolean isRequired() {
        if (type in [Boolean, boolean]) {
            false
        } else if (type == String) {
            // if the property prohibits nulls and blanks are converted to nulls, then blanks will be prohibited even if a blank
            // constraint does not exist
            boolean hasBlankConstraint = constraints?.hasAppliedConstraint(BLANK_CONSTRAINT)
            boolean blanksImplicityProhibited = !hasBlankConstraint && !constraints?.nullable && convertEmptyStringsToNull && trimStrings
            !constraints?.nullable && (!constraints?.blank || blanksImplicityProhibited)
        } else {
            !constraints?.nullable
        }
    }

    List<String> getLabelKeys() {
        List labelKeys = []
        labelKeys.add("${GrailsNameUtils.getPropertyName(beanType.simpleName)}.${name}.label")
        if (rootProperty) {
            labelKeys.add("${GrailsNameUtils.getPropertyName(rootBeanType.simpleName)}.${pathFromRoot}.label".replaceAll(/\[(.+)\]/, ''))
        }
        labelKeys.unique()
    }

    String getDefaultLabel() {
        GrailsNameUtils.getNaturalName(name)
    }

    public int compareTo(DomainProperty o2) {

        if (domainClass.mapping.identifier.identifierName.contains(name)) {
            return -1;
        }
        if (domainClass.mapping.identifier.identifierName.contains(o2.name)) {
            return 1;
        }

        Constrained cp2 = o2.constraints

        if (constraints == null & cp2 == null) {
            return name.compareTo(o2.name);
        }

        if (constraints == null) {
            return 1;
        }

        if (cp2 == null) {
            return -1;
        }

        if (constraints.order > cp2.order) {
            return 1;
        }

        if (constraints.order < cp2.order) {
            return -1;
        }

        return 0;
    }
}
