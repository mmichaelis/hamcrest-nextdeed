package com.github.mmichaelis.hamcrest.nextdeed.image.internal;

import java.io.Serializable;

/**
 * @since SINCE
 */
public interface NumberEqualsFunction extends Serializable {
  Boolean apply(Number n1, Number n2);
}
