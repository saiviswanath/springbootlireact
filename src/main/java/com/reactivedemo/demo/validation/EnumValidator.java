package com.reactivedemo.demo.validation;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.util.StringUtils;

public class EnumValidator implements ConstraintValidator<IEnumValidator, String> {
	
	List<String> valueList = null;

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return !StringUtils.isEmpty(value) && valueList.contains(value.toUpperCase());
	}

	@Override
	public void initialize(IEnumValidator constraintAnnotation) {
		valueList = new ArrayList<>();
        Class<? extends Enum<?>> enumClass = constraintAnnotation.enumClazz();

        @SuppressWarnings("rawtypes")
        Enum[] enumValArr = enumClass.getEnumConstants();

        for (@SuppressWarnings("rawtypes") Enum enumVal : enumValArr) {
            valueList.add(enumVal.toString().toUpperCase());
        }
	}
	
	

}
