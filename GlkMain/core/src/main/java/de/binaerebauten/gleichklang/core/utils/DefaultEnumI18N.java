package de.binaerebauten.gleichklang.core.utils;

import java.util.StringJoiner;

/**
 * Defines the naming scheme for localization of model enum classes.
 * Enums implementing this interface have to override the {@link #toString()} method with:
 * {code}
 * public toString() { return msg(); }
 * {/code}
 * <p/>
 * Unfortunately this can't be enforced by the compiler.
 */
public interface DefaultEnumI18N extends DefaultI18N
{
	@Override String toString();

	/**
	 * The default bundle name for an enum contained in the package PKG is
	 * {PKG}.I18N
	 *
	 * @return the default bundle name for this enum
	 */
	default String getBaseName()
	{
		return this.getClass().getPackage().getName() + ".I18N";
	}

	/**
	 * Returns the key for the given model enum. The key has the format:
	 * {CLAZZ}_ENUM_{ENUM_VALUE}
	 * where {CLAZZ} is the simple enum class name converted to upper case
	 * and {ENUM_VALUE} is the enum name with '_' replaced with ''
	 * <p/>
	 * Example:
	 * For an enum value MyEnum.VALUE_1 the key is "MYENUM_LABEL_VALUE1"
	 *
	 * @return the key for the given enum value
	 */
	default String getKey()
	{
		Enum<?> thisEnum = (Enum<?>) this;
		String keyPrefix = this.getClass().getSimpleName().toUpperCase();
		String keySuffix = thisEnum.name().replace("_", "");

		return new StringJoiner("_")
				.add(keyPrefix)
				.add("ENUM")
				.add(keySuffix)
				.toString();
	}
}

