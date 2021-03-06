package saros.filesystem;

import java.io.IOException;

/**
 * Represents a reference point in the local (virtual) file system.
 *
 * <p>Defines the reference-point-specific default behavior of methods defined by {@link
 * IContainer}.
 *
 * <p>This interface is under development. It is currently a placeholder and will be reworked once
 * the migration to reference points is completed.
 */
public interface IReferencePoint extends IContainer {
  /**
   * Returns an empty path.
   *
   * @return an empty path
   */
  IPath getReferencePointRelativePath();

  /**
   * Always throws an IO exception.
   *
   * <p>Deleting a reference point resource is not supported.
   *
   * @throws IOException always
   */
  default void delete() throws IOException {
    throw new IOException(
        "Deleting reference point resources is not supported - tried to delete " + this);
  }

  /**
   * Always returns <code>null</code>.
   *
   * <p>Resources above the shared reference point can not be described through (conventional)
   * relative paths. Additionally, such resources are not part of the shared scope and should
   * therefore not be of interest for Saros.
   *
   * @return <code>null</code>
   */
  default IContainer getParent() {
    return null;
  }

  /**
   * Returns a reference to itself.
   *
   * @return a reference to itself
   */
  default IReferencePoint getReferencePoint() {
    return this;
  }

  default Type getType() {
    return Type.REFERENCE_POINT;
  }

  /**
   * Returns <code>false</code>.
   *
   * <p>Reference points are the base for all shared resources and can therefore not be ignored.
   *
   * @return <code>false</code>
   */
  default boolean isIgnored() {
    return false;
  }
}
