package grails.plugin.angular.scaffolding.model

import grails.core.GrailsDomainClass
import grails.core.GrailsDomainClassProperty
import grails.plugin.angular.scaffolding.element.PropertyType
import grails.plugin.formfields.BeanPropertyAccessor
import grails.util.GrailsClassUtils
import grails.validation.Constrained
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Embedded
import org.grails.datastore.mapping.model.types.ManyToMany
import org.grails.datastore.mapping.model.types.ManyToOne
import org.grails.datastore.mapping.model.types.OneToMany
import org.grails.datastore.mapping.model.types.OneToOne
import org.grails.validation.DomainClassPropertyComparator
import org.grails.validation.GrailsDomainClassValidator
import org.springframework.beans.factory.annotation.Autowired

import java.sql.Blob

class DomainModelServiceImpl implements DomainModelService {

    @Autowired
    MappingContext grailsDomainClassMappingContext

    List<String> currencyCodes = ['EUR', 'XCD', 'USD', 'XOF', 'NOK', 'AUD',
                                  'XAF', 'NZD', 'MAD', 'DKK', 'GBP', 'CHF',
                                  'XPF', 'ILS', 'ROL', 'TRL']

    List<String> decimalTypes = ['double', 'float', 'bigdecimal']

    List<GrailsDomainClassProperty> getEditableProperties(GrailsDomainClass domainClass) {
        List<GrailsDomainClassProperty> properties = domainClass.persistentProperties as List
        List blacklist = ['dateCreated', 'lastUpdated']
        def scaffoldProp = GrailsClassUtils.getStaticPropertyValue(domainClass.clazz, 'scaffold')
        if (scaffoldProp) {
            blacklist.addAll(scaffoldProp.exclude)
        }
        properties.removeAll { it.name in blacklist }
        properties.removeAll {
            Constrained constraints = (Constrained)domainClass.constrainedProperties[it.name]
            !constraints.display
        }
        properties.removeAll { it.derived }

        sort(properties, domainClass)
        properties
    }

/*    List<PersistentProperty> getEditableProperties(PersistentEntity domainClass) {
        List<PersistentProperty> properties = domainClass.persistentProperties as List
        List blacklist = ['dateCreated', 'lastUpdated']
        def scaffoldProp = GrailsClassUtils.getStaticPropertyValue(domainClass.javaClass, 'scaffold')
        if (scaffoldProp) {
            blacklist.addAll(scaffoldProp.exclude)
        }
        GrailsDomainClass grailsDomainClass = ((GrailsDomainClassValidator) grailsDomainClassMappingContext.getEntityValidator(domainClass)).domainClass

        properties.removeAll { it.name in blacklist }
        properties.removeAll {
            Constrained constraints = (Constrained)grailsDomainClass.constrainedProperties[it.name]
            !constraints.display
        }
        properties.removeAll { it.mapping instanceof PropertyConfig classMapping.mappedForm..properties.derived }

        sort(grailsDomainClass, properties)
        properties
    }*/

    List<GrailsDomainClassProperty> getVisibleProperties(GrailsDomainClass domainClass) {
        List<GrailsDomainClassProperty> properties = domainClass.persistentProperties
        sort(properties, domainClass)
        properties
    }

    List<GrailsDomainClassProperty> getShortListVisibleProperties(GrailsDomainClass domainClass) {
        List<GrailsDomainClassProperty> properties = getVisibleProperties(domainClass)
        if (properties.size() > 6) {
            properties = properties[0..6]
        }
        properties
    }

    void sort(List<GrailsDomainClassProperty> properties, GrailsDomainClass domainClass) {
        Collections.sort(properties, new DomainClassPropertyComparator(domainClass))
    }

/*    void sort(GrailsDomainClass domainClass, List<PersistentProperty> properties) {
        Collections.sort(properties, new PersistentPropertyComparator(domainClass))
    }*/

    protected Boolean isString(Class clazz) {
        clazz in [String, null]
    }

    protected Boolean isBoolean(Class clazz) {
        clazz in [boolean, Boolean]
    }

    protected Boolean isNumber(Class clazz) {
        clazz.isPrimitive() || clazz in Number
    }

    protected Boolean isURL(Class clazz) {
        clazz in URL
    }

    protected Boolean isEnum(Class clazz) {
        clazz.isEnum()
    }

    protected Boolean isDate(Class clazz) {
        clazz in [Date, Calendar, java.sql.Date]
    }

    protected Boolean isTime(Class clazz) {
        clazz in java.sql.Time
    }

    protected Boolean isFile(Class clazz) {
        clazz in [byte[], Byte[], Blob]
    }

    protected Boolean isTimeZone(Class clazz) {
        clazz in TimeZone
    }

    protected Boolean isCurrency(Class clazz) {
        clazz in Currency
    }

    protected Boolean isLocale(Class clazz) {
        clazz in Locale
    }

    PropertyType getPropertyType(BeanPropertyAccessor property) {
        if (isString(property.propertyType)) {
            PropertyType.STRING
        } else if (isBoolean(property.propertyType)) {
            PropertyType.BOOLEAN
        } else if (isNumber(property.propertyType)) {
            PropertyType.NUMBER
        } else if (isURL(property.propertyType)) {
            PropertyType.URL
        } else if (isEnum(property.propertyType)) {
            PropertyType.ENUM
        } else if (property.persistentProperty?.oneToOne || property.persistentProperty?.manyToOne || property.persistentProperty?.manyToMany) {
            PropertyType.ASSOCIATION
        } else if (property.persistentProperty?.oneToMany) {
            PropertyType.ONETOMANY
        } else if (isDate(property.propertyType)) {
            PropertyType.DATE
        } else if (isTime(property.propertyType)) {
            PropertyType.TIME
        } else if (isFile(property.propertyType)) {
            PropertyType.FILE
        } else if (isTimeZone(property.propertyType)) {
            PropertyType.TIMEZONE
        } else if (isCurrency(property.propertyType)) {
            PropertyType.CURRENCY
        } else if (isLocale(property.propertyType)) {
            PropertyType.LOCALE
        }
    }

    PropertyType getPropertyType(GrailsDomainClassProperty property) {
        if (isString(property.type)) {
            PropertyType.STRING
        } else if (isBoolean(property.type)) {
            PropertyType.BOOLEAN
        } else if (isNumber(property.type)) {
            PropertyType.NUMBER
        } else if (isURL(property.type)) {
            PropertyType.URL
        } else if (isEnum(property.type)) {
            PropertyType.ENUM
        } else if (property.oneToOne || property.manyToOne || property.manyToMany) {
            PropertyType.ASSOCIATION
        } else if (property.oneToMany) {
            PropertyType.ONETOMANY
        } else if (isDate(property.type)) {
            PropertyType.DATE
        } else if (isTime(property.type)) {
            PropertyType.TIME
        } else if (isFile(property.type)) {
            PropertyType.FILE
        } else if (isTimeZone(property.type)) {
            PropertyType.TIMEZONE
        } else if (isCurrency(property.type)) {
            PropertyType.CURRENCY
        } else if (isLocale(property.type)) {
            PropertyType.LOCALE
        }
    }

   /* PropertyType getPropertyType(PersistentProperty property) {
        if (isString(property.type)) {
            PropertyType.STRING
        } else if (isBoolean(property.type)) {
            PropertyType.BOOLEAN
        } else if (isNumber(property.type)) {
            PropertyType.NUMBER
        } else if (isURL(property.type)) {
            PropertyType.URL
        } else if (isEnum(property.type)) {
            PropertyType.ENUM
        } else if (property instanceof OneToOne || property instanceof ManyToOne || property instanceof ManyToMany) {
            PropertyType.ASSOCIATION
        } else if (property instanceof OneToMany) {
            PropertyType.ONETOMANY
        } else if (isDate(property.type)) {
            PropertyType.DATE
        } else if (isTime(property.type)) {
            PropertyType.TIME
        } else if (isFile(property.type)) {
            PropertyType.FILE
        } else if (isTimeZone(property.type)) {
            PropertyType.TIMEZONE
        } else if (isCurrency(property.type)) {
            PropertyType.CURRENCY
        } else if (isLocale(property.type)) {
            PropertyType.LOCALE
        }
    }*/
    
    protected Boolean hasProperty(GrailsDomainClass domainClass, Closure closure) {
        getEditableProperties(domainClass).any {
            if (it.embedded) {
                hasProperty(it.component, closure)
            } else {
                closure.call(it)
            }
        }
    }

    Boolean hasPropertyType(GrailsDomainClass domainClass, PropertyType propertyType) {
        hasProperty(domainClass) { GrailsDomainClassProperty property ->
            getPropertyType(property) == propertyType
        }
    }

/*    protected Boolean hasProperty(PersistentEntity domainClass, Closure closure) {
        getEditableProperties(domainClass).any {
            if (it instanceof Embedded) {
                hasProperty(it.associatedEntity, closure)
            } else {
                closure.call(it)
            }
        }
    }

    Boolean hasPropertyType(PersistentEntity domainClass, PropertyType propertyType) {
        hasProperty(domainClass) { GrailsDomainClassProperty property ->
            getPropertyType(property) == propertyType
        }
    }*/

    protected String formatTimeZone(TimeZone timeZone) {
        Date date = new Date()
        String shortName = timeZone.getDisplayName(timeZone.inDaylightTime(date), TimeZone.SHORT)
        String longName = timeZone.getDisplayName(timeZone.inDaylightTime(date), TimeZone.LONG)

        int offset = timeZone.rawOffset
        def hour = offset / (60 * 60 * 1000)
        def min = Math.abs(offset / (60 * 1000)) % 60

        "${shortName}, ${longName} ${hour}:${min} [${timeZone.ID}]"
    }
    
    protected String formatLocale(Locale locale) {
        locale.country ? "${locale.language}, ${locale.country},  ${locale.displayName}" : "${locale.language}, ${locale.displayName}"
    }

    Map<String, String> getTimeZones() {
        TimeZone.availableIDs.collectEntries {
            [(it): formatTimeZone(TimeZone.getTimeZone(it))]
        }
    }
    
    Map<String, String> getLocales() {
        Locale.availableLocales.collectEntries {
            if (it.country || it.language) {
                String key = it.country ? "${it.language}_${it.country}" : it.language
                [(key): formatLocale(it)]
            } else {
                [:]
            }
        }
    }
}
