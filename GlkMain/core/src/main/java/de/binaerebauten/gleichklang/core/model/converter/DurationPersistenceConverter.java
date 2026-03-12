package de.binaerebauten.gleichklang.core.model.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Converter(autoApply = true)
public class DurationPersistenceConverter implements AttributeConverter<Duration, Long>
{
	@Override
	public Long convertToDatabaseColumn(Duration duration)
	{
		return duration == null ? null : duration.toMillis();
	}
	
	@Override
	public Duration convertToEntityAttribute(Long databaseValue)
	{
		return databaseValue == null ? null : Duration.of(databaseValue, ChronoUnit.MILLIS);
	}
}
