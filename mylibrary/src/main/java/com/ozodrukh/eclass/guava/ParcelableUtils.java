package com.ozodrukh.eclass.guava;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class that helpes write lists, maps and so on (unsupported types) to
 * {@link Parcelable}
 */
public class ParcelableUtils {
  static final int NULL_OBJECT = 0;
  static final int NORMAL_OBJECT = 1;

  /* Some objects may not have Parcelable implementation due some limitations or third party
   * software, this is map of this objects with parcelable implementation */
  private static final Map<Class, ParcelableObject<?>> OBJECT_WRITERS = new HashMap<>();

  /**
   * Register object to be able write/read from {@link Parcel} that has no
   * {@link Parcelable} implementation
   *
   * @param unimplementedParcelableClass Unimplemented class
   * @param implementation Parcelable implementation of not parcelable object
   */
  public static <T> void register(Class<T> unimplementedParcelableClass,
      ParcelableObject<T> implementation) {
    OBJECT_WRITERS.put(unimplementedParcelableClass, implementation);
  }

  /**
   * Missing functionality inside {@link Parcel} object to read/write
   * {@link Date} objects
   *
   * @param value Date object to write into parcel
   * @see #readDate(Parcel)
   */
  public static void writeDate(Parcel parcel, Date value) {
    parcel.writeLong(value == null ? NULL_OBJECT : value.getTime());
  }

  /**
   * Missing functionality inside {@link Parcel} object to read/write
   * {@link Date} objects
   *
   * @return previously written {@link Date} object
   * @see #writeDate(Parcel, Date)
   */
  public static Date readDate(Parcel parcel) {
    long time = parcel.readLong();
    return time > 0 ? new Date(time) : null;
  }

  public static <T> void writeNullableParcelable(T object, Parcel parcel, int flags) {
    if (object == null) {
      parcel.writeInt(NULL_OBJECT);
    } else {
      parcel.writeInt(NORMAL_OBJECT);
      if (object instanceof Parcelable) {
        ((Parcelable) object).writeToParcel(parcel, flags);
      } else {
        //noinspection unchecked
        ParcelableObject<T> impl = (ParcelableObject<T>) OBJECT_WRITERS.get(object.getClass());

        if (impl != null) {
          impl.writeToParcel(object, parcel, flags);
        } else {
          throw new NullPointerException(
              "Parcelable implementation not found for " + object.getClass().getSimpleName());
        }
      }
    }
  }

  public static <T> T readNullableParcelable(Parcelable.Creator<T> creator, Parcel parcel) {
    if (parcel.readInt() == NORMAL_OBJECT) {
      return creator.createFromParcel(parcel);
    }
    return null;
  }

  /**
   * Writes list of parcelable into parcel, this method is more reliable than
   * system's standart version
   *
   * @param data The list of parcelables to write
   * @param parcel The parcel source to write
   * @param flags additional flags
   */
  public static <E> void writeList(List<E> data, Parcel parcel, int flags) {
    final int N = data == null ? NULL_OBJECT : data.size();
    parcel.writeInt(N);

    for (int i = 0; i < N; i++) {
      writeNullableParcelable(data.get(i), parcel, flags);
    }
  }

  /**
   * Creates an {@link ArrayList<E> } and restores previously written data,
   * if no data was written, returns the empty list
   *
   * @param parcel The parcel source
   * @param creator Parcelable creator
   */
  public static <E extends Parcelable> List<E> readList(Parcel parcel,
      Parcelable.Creator<E> creator) {
    final int N = parcel.readInt();
    final ArrayList<E> data = new ArrayList<>(N);
    readList(data, N, parcel, creator);
    return data;
  }

  /**
   * Creates an {@link ArrayList<E> } and restores previously written data,
   * if no data was written, returns the empty list
   *
   * @param N Size of old list, pass -1 if you don't know
   * @param source The parcel source
   * @param creator Parcelable creator
   */
  public static <E> void readList(List<E> data, int N, Parcel source,
      Parcelable.Creator<E> creator) {
    if (N == -1) {
      N = source.readInt();
    }

    for (int i = 0; i < N; i++) {
      data.add(readNullableParcelable(creator, source));
    }
  }

  public interface ParcelableObject<T> {
    void writeToParcel(T value, Parcel dest, int flags);
  }
}
