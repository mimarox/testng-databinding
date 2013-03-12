package net.sf.testng.databinding.beans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.sf.testng.databinding.util.modules.VerifyingModule;


/**
 * Defines the {@link VerifyingModule} to be used to verify the field or the return value of the
 * method annotated with this annotation.
 * <p>
 * Using this annotation is only possible within beans used as test data containers.
 * <p>
 * It's the responsibility of the receiving test method or intermediate framework code like the
 * table verification code of the TestNG Selenium integration framework to apply the specified
 * verifying module.
 * 
 * @author Matthias Rothe
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Verifier {

	/**
	 * @return The verifying module class to be used
	 */
	Class<? extends VerifyingModule<?>> value();
}
