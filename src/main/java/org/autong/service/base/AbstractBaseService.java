package org.autong.service.base;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.autong.config.Settings;

/**
 * Abstract AbstractBaseService class.
 *
 * @version 1.0.4
 * @since 1.0.4
 */
@Getter
@SuppressWarnings("ClassTypeParameterName")
public abstract class AbstractBaseService<Type> implements BaseService<Type> {
  private final Settings settings;
  private Settings updatedSettings;
  private JsonObject expectedResult;

  @Setter(AccessLevel.PROTECTED)
  private Consumer<Validator> validator;

  /**
   * Constructor for AbstractBaseService.
   *
   * @param settings a {@link org.autong.config.Settings} object
   * @since 1.0.5
   */
  protected AbstractBaseService(Settings settings) {
    this.settings = settings;
    this.reset();
  }

  /**
   * reset.
   *
   * @since 1.0.5
   */
  public void reset() {
    this.updatedSettings = null;
    this.expectedResult = null;
    this.validator = null;
  }

  /** {@inheritDoc} */
  @SuppressWarnings("unchecked")
  public Type withSettings(Settings settings) {
    this.updatedSettings = settings;
    return (Type) this;
  }

  /** {@inheritDoc} */
  @SuppressWarnings("unchecked")
  public Type withValidator(Consumer<Validator> validator) {
    this.validator = validator;
    return (Type) this;
  }

  /** {@inheritDoc} */
  @SuppressWarnings("unchecked")
  public Type withExpectedResult(JsonObject expected) {
    this.expectedResult = expected;
    return (Type) this;
  }
}
