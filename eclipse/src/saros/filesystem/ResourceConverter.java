package saros.filesystem;

import java.util.Objects;
import java.util.Set;
import org.eclipse.core.runtime.Path;

/** Utility class providing static methods to convert between Eclipse and Saros Resource objects. */
public class ResourceConverter {
  /**
   * Converts the given string into an IPath object.
   *
   * @param pathString the path string to convert
   * @return an IPath object representing the given string path
   * @throws NullPointerException if the given string is <code>null</code> or the path represented
   *     by the string is absolute
   */
  public static IPath convertToPath(String pathString) {
    Objects.requireNonNull(pathString, "Given string must not be null");

    Path path = new Path(pathString);

    return convertToPath(path);
  }

  /**
   * Returns an IPath object representing the given Eclipse delegate.
   *
   * @param eclipseDelegate the Eclipse path delegate
   * @return an IPath object representing the given Eclipse delegate
   * @throws NullPointerException if the given path is <code>null</code> or is absolute
   */
  public static IPath convertToPath(org.eclipse.core.runtime.IPath eclipseDelegate) {
    Objects.requireNonNull(eclipseDelegate, "Given Eclipse path must not be null");

    if (eclipseDelegate.isAbsolute()) {
      throw new IllegalArgumentException("Given path must not be absolute - " + eclipseDelegate);
    }

    return new EclipsePathImpl(eclipseDelegate);
  }

  /**
   * Returns the resource representing the given resource delegate.
   *
   * <p>Returns the reference point if the given delegate is represented by one of the given
   * reference points.
   *
   * @param referencePoints the shared reference points
   * @param resourceDelegate the resource delegate for which to get a resource
   * @return the resource representing the given resource delegate or <code>null</code> if the given
   *     delegate does not belong to the given reference point
   * @throws NullPointerException if the given set of reference points or resource delegate is
   *     <code>null</code>
   */
  public static IResource convertToResource(
      Set<IReferencePoint> referencePoints, org.eclipse.core.resources.IResource resourceDelegate) {

    Objects.requireNonNull(referencePoints, "Given set of reference points must not be null");

    for (IReferencePoint referencePoint : referencePoints) {
      IResource resource = convertToResource(referencePoint, resourceDelegate);

      if (resource != null) {
        return resource;
      }
    }

    return null;
  }

  /**
   * Returns the resource representing the given resource delegate.
   *
   * <p>Returns the given reference point if the given delegate is represented by the reference
   * point.
   *
   * @param referencePoint a shared reference point
   * @param resourceDelegate the resource delegate for which to get a resource
   * @return the resource representing the given resource delegate or <code>null</code> if the given
   *     delegate does not belong to the given reference point
   * @throws NullPointerException if the given reference points or resource delegate is <code>null
   *     </code>
   */
  public static IResource convertToResource(
      IReferencePoint referencePoint, org.eclipse.core.resources.IResource resourceDelegate) {

    Objects.requireNonNull(referencePoint, "Given reference point must not be null");

    EclipseReferencePointImpl eclipseReferencePoint = (EclipseReferencePointImpl) referencePoint;

    if (eclipseReferencePoint.getDelegate().equals(resourceDelegate)) {
      return referencePoint;
    }

    return eclipseReferencePoint.getResource(resourceDelegate);
  }

  /**
   * Returns the file representing the given file delegate.
   *
   * @param referencePoints the shared reference points
   * @param fileDelegate the file delegate for which to get a file
   * @return the file representing the given file delegate or <code>null</code> if the given
   *     delegate does not belong to the given reference point
   * @throws NullPointerException if the given set of reference points or file delegate is <code>
   *     null</code>
   */
  public static IFile convertToFile(
      Set<IReferencePoint> referencePoints, org.eclipse.core.resources.IFile fileDelegate) {

    return (IFile) convertToResource(referencePoints, fileDelegate);
  }

  /**
   * Returns the file representing the given file delegate.
   *
   * @param referencePoint a shared reference point
   * @param fileDelegate the file delegate for which to get a file
   * @return the file representing the given file delegate or <code>null</code> if the given
   *     delegate does not belong to the given reference point
   * @throws NullPointerException if the given reference points or file delegate is <code>null
   *     </code>
   */
  public static IFile convertToFile(
      IReferencePoint referencePoint, org.eclipse.core.resources.IFile fileDelegate) {

    return (IFile) convertToResource(referencePoint, fileDelegate);
  }

  /**
   * Returns the container representing the given container delegate.
   *
   * <p>Returns the reference point if the given delegate is represented by one of the given
   * reference points.
   *
   * @param referencePoints the shared reference points
   * @param containerDelegate the container delegate for which to get a container
   * @return the container representing the given container delegate or <code>null</code> if the
   *     given delegate does not belong to the given reference point
   * @throws NullPointerException if the given set of reference points or container delegate is
   *     <code>null</code>
   */
  public static IContainer convertToContainer(
      Set<IReferencePoint> referencePoints,
      org.eclipse.core.resources.IContainer containerDelegate) {

    return (IContainer) convertToResource(referencePoints, containerDelegate);
  }

  /**
   * Returns the container representing the given container delegate.
   *
   * <p>Returns the given reference point if the given delegate is represented by the reference
   * point.
   *
   * @param referencePoint a shared reference point
   * @param containerDelegate the container delegate for which to get a container
   * @return the container representing the given container delegate or <code>null</code> if the
   *     given delegate does not belong to the given reference point
   * @throws NullPointerException if the given reference points or container delegate is <code>null
   *     </code>
   */
  public static IContainer convertToContainer(
      IReferencePoint referencePoint, org.eclipse.core.resources.IContainer containerDelegate) {

    return (IContainer) convertToResource(referencePoint, containerDelegate);
  }

  /**
   * Returns the Eclipse delegate represented by the given an IPath object.
   *
   * @param path the path whose delegate to get
   * @return the Eclipse delegate represented by the given an IPath object
   * @throws NullPointerException if the given path is <code>null</code>
   */
  public static org.eclipse.core.runtime.IPath getDelegate(IPath path) {
    Objects.requireNonNull(path, "Given path must not be null");

    EclipsePathImpl eclipsePath = (EclipsePathImpl) path;

    return eclipsePath.getDelegate();
  }

  /**
   * Returns the Eclipse delegate represented by the given resource.
   *
   * @param resource the resource whose delegate to get
   * @return the Eclipse delegate represented by the given resource
   * @throws NullPointerException if the given resource is <code>null</code>
   */
  public static org.eclipse.core.resources.IResource getDelegate(IResource resource) {
    Objects.requireNonNull(resource, "Given resource must not be null");

    switch (resource.getType()) {
      case REFERENCE_POINT:
        return getDelegate((IReferencePoint) resource);

      case FILE:
        return getDelegate((IFile) resource);

      case FOLDER:
        return getDelegate((IFolder) resource);

      default:
        throw new IllegalStateException(
            "Encountered unhandled resource type " + resource.getType() + " - " + resource);
    }
  }

  /**
   * Returns the Eclipse delegate represented by the given reference point.
   *
   * @param referencePoint the reference point whose delegate to get
   * @return the Eclipse delegate represented by the given reference point
   * @throws NullPointerException if the given reference point is <code>null</code>
   */
  public static org.eclipse.core.resources.IContainer getDelegate(IReferencePoint referencePoint) {
    Objects.requireNonNull(referencePoint, "Given reference point must not be null");

    EclipseReferencePointImpl eclipseReferencePoint = (EclipseReferencePointImpl) referencePoint;

    return eclipseReferencePoint.getDelegate();
  }

  /**
   * Returns the Eclipse delegate represented by the given file.
   *
   * @param file the file whose delegate to get
   * @return the Eclipse delegate represented by the given file
   * @throws NullPointerException if the given file is <code>null</code>
   */
  public static org.eclipse.core.resources.IFile getDelegate(IFile file) {
    Objects.requireNonNull(file, "Given file must not be null");

    EclipseFileImplV2 eclipseFile = (EclipseFileImplV2) file;

    return eclipseFile.getDelegate();
  }

  /**
   * Returns the Eclipse delegate represented by the given folder.
   *
   * @param folder the folder whose delegate to get
   * @return the Eclipse delegate represented by the given folder
   * @throws NullPointerException if the given folder is <code>null</code>
   */
  public static org.eclipse.core.resources.IFolder getDelegate(IFolder folder) {
    Objects.requireNonNull(folder, "Given folder must not be null");

    EclipseFolderImplV2 eclipseFile = (EclipseFolderImplV2) folder;

    return eclipseFile.getDelegate();
  }
}
