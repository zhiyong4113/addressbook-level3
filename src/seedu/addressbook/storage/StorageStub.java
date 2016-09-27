package seedu.addressbook.storage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import seedu.addressbook.data.AddressBook;
import seedu.addressbook.data.exception.IllegalValueException;
import seedu.addressbook.storage.StorageFile.InvalidStorageFilePathException;
import seedu.addressbook.storage.StorageFile.StorageOperationException;
import seedu.addressbook.storage.jaxb.AdaptedAddressBook;

public class StorageStub extends Storage {
	/** Default file path used if the user doesn't provide the file name. */
	public static final String DEFAULT_STORAGE_FILEPATH = "addressbook.txt";

	private final JAXBContext jaxbContext;

	public final Path path;

	/**
	 * @throws InvalidStorageFilePathException
	 *             if the default path is invalid
	 */
	public StorageStub() throws InvalidStorageFilePathException {
		this(DEFAULT_STORAGE_FILEPATH);
	}

	/**
	 * @throws InvalidStorageFilePathException
	 *             if the given file path is invalid
	 */
	public StorageStub(String filePath) throws InvalidStorageFilePathException {
		try {
			jaxbContext = JAXBContext.newInstance(AdaptedAddressBook.class);
		} catch (JAXBException jaxbe) {
			throw new RuntimeException("jaxb initialisation error");
		}

		path = Paths.get(filePath);
		if (!isValidPath(path)) {
			throw new InvalidStorageFilePathException("Storage file should end with '.txt'");
		}
	}

	/**
	 * Returns true if the given path is acceptable as a storage file. The file
	 * path is considered acceptable if it ends with '.txt'
	 */
	private static boolean isValidPath(Path filePath) {
		return filePath.toString().endsWith(".txt");
	}

	@Override
	public AddressBook load() throws StorageOperationException {
		try (final Reader fileReader = new BufferedReader(new FileReader(path.toFile()))) {

			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			final AdaptedAddressBook loaded = (AdaptedAddressBook) unmarshaller.unmarshal(fileReader);
			// manual check for missing elements
			if (loaded.isAnyRequiredFieldMissing()) {
				throw new StorageOperationException("File data missing some elements");
			}
			return loaded.toModelType();

			/*
			 * Note: Here, we are using an exception to create the file if it is
			 * missing. However, we should minimize using exceptions to
			 * facilitate normal paths of execution. If we consider the missing
			 * file as a 'normal' situation (i.e. not truly exceptional) we
			 * should not use an exception to handle it.
			 */

			// create empty file if not found
		} catch (FileNotFoundException fnfe) {
			final AddressBook empty = new AddressBook();
			save(empty);
			return empty;

			// other errors
		} catch (IOException ioe) {
			throw new StorageOperationException("Error writing to file: " + path);
		} catch (JAXBException jaxbe) {
			throw new StorageOperationException("Error parsing file data format");
		} catch (IllegalValueException ive) {
			throw new StorageOperationException("File contains illegal data values; data type constraints not met");
		}
	}

	@Override
	public String getPath() {
		return path.toString();
	}

	@Override
	public void save(AddressBook addressBook) throws StorageOperationException {
		// TODO Auto-generated method stub

	}

}
