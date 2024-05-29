package org.autong.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.underscore.U;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.restassured.path.json.JsonPath;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.autong.enums.DataType;
import org.autong.enums.EncodingType;
import org.autong.exception.CoreException;
import org.json.CDL;
import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.yaml.snakeyaml.Yaml;

/**
 * DataUtil class.
 *
 * @version 1.0.1
 * @since 1.0.1
 */
public class DataUtil<T> extends U<T> {
  @Getter
  private static final Gson gson =
      new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

  /**
   * Constructor for DataUtil.
   *
   * @param iterable a {@link java.lang.Iterable} object
   * @since 1.0.8
   */
  public DataUtil(Iterable<T> iterable) {
    super(iterable);
  }

  /**
   * Constructor for DataUtil.
   *
   * @param string a {@link java.lang.String} object
   * @since 1.0.8
   */
  public DataUtil(String string) {
    super(string);
  }

  /**
   * read.
   *
   * @param resourcePath a {@link java.lang.String} object
   * @param dataType a {@link org.autong.enums.DataType} object
   * @return a {@link com.google.gson.JsonObject} object
   * @since 1.0.8
   */
  public static JsonObject read(String resourcePath, DataType dataType) {
    return read(resourcePath, dataType, EncodingType.STANDARD);
  }

  /**
   * read.
   *
   * @param resourcePath a {@link java.lang.String} object
   * @param dataType a {@link org.autong.enums.DataType} object
   * @param encodingType a {@link org.autong.enums.EncodingType} object
   * @return a {@link com.google.gson.JsonObject} object
   * @since 1.0.8
   */
  public static JsonObject read(String resourcePath, DataType dataType, EncodingType encodingType) {
    URL url = Resources.getResource(resourcePath);
    return read(url, dataType, encodingType);
  }

  /**
   * read.
   *
   * @param resourceUrl a {@link java.net.URL} object
   * @param dataType a {@link org.autong.enums.DataType} object
   * @return a {@link com.google.gson.JsonObject} object
   * @since 1.0.8
   */
  public static JsonObject read(URL resourceUrl, DataType dataType) {
    return read(resourceUrl, dataType, EncodingType.STANDARD);
  }

  /**
   * read.
   *
   * @param resourceUrl a {@link java.net.URL} object
   * @param dataType a {@link org.autong.enums.DataType} object
   * @param encodingType a {@link org.autong.enums.EncodingType} object
   * @return a {@link com.google.gson.JsonObject} object
   * @since 1.0.8
   */
  public static JsonObject read(URL resourceUrl, DataType dataType, EncodingType encodingType) {
    try {
      String payloadString = Resources.toString(resourceUrl, StandardCharsets.UTF_8);

      if (encodingType.equals(EncodingType.BASE64)) {
        return read(IOUtils.toInputStream(payloadString), dataType, encodingType);
      }
      JsonObject jsonObject;
      switch (dataType) {
        case YAML -> {
          Object data = new Yaml().load(payloadString);
          jsonObject = gson.toJsonTree(data).getAsJsonObject();
        }
        case JSON -> jsonObject = gson.fromJson(payloadString, JsonObject.class);
        case XML -> {
          String jsonString = xmlToJson(payloadString);
          jsonObject = gson.fromJson(jsonString, JsonObject.class);
        }
        case CSV -> {
          String jsonString = csvToJson(payloadString);
          jsonObject = new JsonObject();
          jsonObject.add("myArrayList", toJsonElement(jsonString));
        }
        case XLSX -> jsonObject = excelToJson(resourceUrl.getPath());
        case PDF -> jsonObject = pdfToJson(resourceUrl);
        default -> jsonObject = null;
      }

      return extendData(resourceUrl, jsonObject, dataType);
    } catch (IOException ex) {
      throw new CoreException(ex);
    }
  }

  /**
   * read.
   *
   * @param stream a {@link java.io.InputStream} object
   * @param dataType a {@link org.autong.enums.DataType} object
   * @return a {@link com.google.gson.JsonObject} object
   * @since 1.0.8
   */
  public static JsonObject read(InputStream stream, DataType dataType) {
    return read(stream, dataType, EncodingType.STANDARD);
  }

  /**
   * read.
   *
   * @param stream a {@link java.io.InputStream} object
   * @param dataType a {@link org.autong.enums.DataType} object
   * @param encodingType a {@link org.autong.enums.EncodingType} object
   * @return a {@link com.google.gson.JsonObject} object
   * @since 1.0.8
   */
  public static JsonObject read(InputStream stream, DataType dataType, EncodingType encodingType) {
    try {
      JsonObject jsonObject;
      if (encodingType.equals(EncodingType.BASE64)) {
        stream =
            new ByteArrayInputStream(
                decodeBase64(IOUtils.toString(stream, StandardCharsets.UTF_8)));
      }

      switch (dataType) {
        case YAML -> {
          Object data = new Yaml().load(stream);
          jsonObject = gson.toJsonTree(data).getAsJsonObject();
        }
        case JSON -> jsonObject =
            gson.fromJson(IOUtils.toString(stream, StandardCharsets.UTF_8), JsonObject.class);
        case XML -> {
          String jsonString = xmlToJson(IOUtils.toString(stream, StandardCharsets.UTF_8));
          jsonObject = gson.fromJson(jsonString, JsonObject.class);
        }
        case CSV -> {
          String jsonString = csvToJson(IOUtils.toString(stream, StandardCharsets.UTF_8));
          jsonObject = new JsonObject();
          jsonObject.add("myArrayList", toJsonElement(jsonString));
        }
        case XLSX -> jsonObject = excelToJson(stream);
        case PDF -> jsonObject = pdfToJson(ByteStreams.toByteArray(stream));

        default -> jsonObject = null;
      }

      return extendData(null, jsonObject, dataType);
    } catch (IOException ex) {
      throw new CoreException(ex);
    }
  }

  /**
   * write.
   *
   * @param resourcePath a {@link java.lang.String} object
   * @param data a {@link com.google.gson.JsonObject} object
   * @param dataType a {@link org.autong.enums.DataType} object
   * @return a {@link java.net.URL} object
   * @since 1.0.8
   */
  public static URL write(String resourcePath, JsonObject data, DataType dataType) {
    URL url = null;
    switch (dataType) {
      case YAML -> url = createAndWriteToFile(resourcePath, jsonToYaml(data.toString()));
      case JSON -> url = createAndWriteToFile(resourcePath, getGson().toJson(data));
      case XML -> url = createAndWriteToFile(resourcePath, jsonToXml(data.toString()));
      case CSV -> url = createAndWriteToFile(resourcePath, jsonToCsv(data));
      case XLSX -> url = createAndWriteJsonToExcel(resourcePath, data);
      case PDF -> throw new CoreException("PDF writing not implemented");
      default -> throw new CoreException(dataType.name() + " writing not implemented");
    }
    return url;
  }

  /**
   * getAsJsonElement.
   *
   * @param jsonString a {@link java.lang.String} object
   * @param path a {@link java.lang.String} object
   * @return a {@link com.google.gson.JsonElement} object
   * @since 1.0.8
   */
  public static JsonElement getAsJsonElement(String jsonString, String path) {
    JsonPath jsonPath = JsonPath.from(jsonString);
    Object property = jsonPath.get(path);
    return null != property ? gson.toJsonTree(property) : JsonNull.INSTANCE;
  }

  /**
   * getAsJsonElement.
   *
   * @param jsonObject a {@link com.google.gson.JsonObject} object
   * @param path a {@link java.lang.String} object
   * @return a {@link com.google.gson.JsonElement} object
   * @since 1.0.8
   */
  public static JsonElement getAsJsonElement(JsonObject jsonObject, String path) {
    String jsonString = gson.toJson(jsonObject);
    return getAsJsonElement(jsonString, path);
  }

  /**
   * getAsJsonElement.
   *
   * @param data a {@link java.lang.String} object
   * @param path a {@link java.lang.String} object
   * @param dataType a {@link org.autong.enums.DataType} object
   * @return a {@link com.google.gson.JsonElement} object
   * @since 1.0.8
   */
  public static JsonElement getAsJsonElement(String data, String path, DataType dataType) {
    JsonObject jsonObject = read(IOUtils.toInputStream(data, Charset.defaultCharset()), dataType);
    return getAsJsonElement(jsonObject, path);
  }

  /**
   * getAsJsonObject.
   *
   * @param jsonObject a {@link com.google.gson.JsonObject} object
   * @param path a {@link java.lang.String} object
   * @return a {@link com.google.gson.JsonObject} object
   * @since 1.0.8
   */
  public static JsonObject getAsJsonObject(JsonObject jsonObject, String path) {
    JsonElement element = getAsJsonElement(jsonObject, path);
    return element != JsonNull.INSTANCE ? element.getAsJsonObject() : null;
  }

  /**
   * getAsJsonArray.
   *
   * @param jsonObject a {@link com.google.gson.JsonObject} object
   * @param path a {@link java.lang.String} object
   * @return a {@link com.google.gson.JsonArray} object
   * @since 1.0.8
   */
  public static JsonArray getAsJsonArray(JsonObject jsonObject, String path) {
    JsonElement element = getAsJsonElement(jsonObject, path);
    return element != JsonNull.INSTANCE ? element.getAsJsonArray() : null;
  }

  /**
   * getAsString.
   *
   * @param jsonObject a {@link com.google.gson.JsonObject} object
   * @param path a {@link java.lang.String} object
   * @return a {@link java.lang.String} object
   * @since 1.0.8
   */
  public static String getAsString(JsonObject jsonObject, String path) {
    JsonElement element = getAsJsonElement(jsonObject, path);
    return element != JsonNull.INSTANCE ? element.getAsString() : null;
  }

  /**
   * toJsonElement.
   *
   * @param object a {@link java.lang.Object} object
   * @return a {@link com.google.gson.JsonElement} object
   * @since 1.0.8
   */
  public static JsonElement toJsonElement(Object object) {
    return Optional.ofNullable(object)
        .map(
            data ->
                data instanceof String json
                    ? gson.fromJson(json, JsonElement.class)
                    : gson.toJsonTree(data))
        .orElse(JsonNull.INSTANCE);
  }

  /**
   * toJsonObject.
   *
   * @param object a {@link java.lang.Object} object
   * @return a {@link com.google.gson.JsonObject} object
   * @since 1.0.8
   */
  public static JsonObject toJsonObject(Object object) {
    return toJsonElement(object).getAsJsonObject();
  }

  /**
   * toJsonArray.
   *
   * @param object a {@link java.lang.Object} object
   * @return a {@link com.google.gson.JsonArray} object
   * @since 1.0.8
   */
  public static JsonArray toJsonArray(Object object) {
    return toJsonElement(object).getAsJsonArray();
  }

  /**
   * toObject.
   *
   * @param jsonString a {@link java.lang.String} object
   * @param classType a {@link java.lang.Class} object
   * @param <O> a O class
   * @return a O object
   * @since 1.0.8
   */
  public static <O> O toObject(String jsonString, Class<O> classType) {
    return toObject(toJsonObject(jsonString), classType);
  }

  /**
   * toObject.
   *
   * @param jsonObject a {@link com.google.gson.JsonObject} object
   * @param classType a {@link java.lang.Class} object
   * @param <O> a O class
   * @return a O object
   * @since 1.0.8
   */
  public static <O> O toObject(JsonObject jsonObject, Class<O> classType) {
    return getGson().fromJson(jsonObject, classType);
  }

  /**
   * toObjectArray.
   *
   * @param jsonString a {@link java.lang.String} object
   * @param classType a {@link java.lang.Class} object
   * @param <O> a O class
   * @return a {@link java.util.List} object
   * @since 1.0.8
   */
  public static <O> List<O> toObjectArray(String jsonString, Class<O> classType) {
    return toObjectArray(toJsonArray(jsonString), classType);
  }

  /**
   * toObjectArray.
   *
   * @param jsonArray a {@link com.google.gson.JsonArray} object
   * @param classType a {@link java.lang.Class} object
   * @param <O> a O class
   * @return a {@link java.util.List} object
   * @since 1.0.8
   */
  public static <O> List<O> toObjectArray(JsonArray jsonArray, Class<O> classType) {
    Type arrayType = TypeToken.getParameterized(ArrayList.class, classType).getType();
    return getGson().fromJson(jsonArray, arrayType);
  }

  /**
   * xmlToJsonObject.
   *
   * @param xml a {@link java.lang.String} object
   * @return a {@link com.google.gson.JsonObject} object
   * @since 1.0.8
   */
  public static JsonObject xmlToJsonObject(String xml) {
    return getGson().fromJson(xmlToJson(xml), JsonObject.class);
  }

  /**
   * jsonObjectToXml.
   *
   * @param jsonObject a {@link com.google.gson.JsonObject} object
   * @return a {@link java.lang.String} object
   * @since 1.0.8
   */
  public static String jsonObjectToXml(JsonObject jsonObject) {
    return jsonToXml(getGson().toJson(jsonObject));
  }

  /**
   * jsonObjectToXml.
   *
   * @param jsonObject a {@link com.google.gson.JsonObject} object
   * @param mode a Mode object
   * @return a {@link java.lang.String} object
   * @since 1.0.8
   */
  public static String jsonObjectToXml(JsonObject jsonObject, Mode mode) {
    return jsonToXml(getGson().toJson(jsonObject), mode);
  }

  /**
   * jsonArrayToXml.
   *
   * @param jsonArray a {@link com.google.gson.JsonArray} object
   * @return a {@link java.lang.String} object
   * @since 1.0.8
   */
  public static String jsonArrayToXml(JsonArray jsonArray) {
    return jsonToXml(getGson().toJson(jsonArray));
  }

  /**
   * jsonArrayToXml.
   *
   * @param jsonArray a {@link com.google.gson.JsonArray} object
   * @param mode a Mode object
   * @return a {@link java.lang.String} object
   * @since 1.0.8
   */
  public static String jsonArrayToXml(JsonArray jsonArray, Mode mode) {
    return jsonToXml(getGson().toJson(jsonArray), mode);
  }

  /**
   * deepMerge.
   *
   * @param source a {@link com.google.gson.JsonObject} object
   * @param target a {@link com.google.gson.JsonObject} object
   * @return a {@link com.google.gson.JsonObject} object
   * @since 1.0.8
   */
  public static JsonObject deepMerge(JsonObject source, JsonObject target) {
    if (source == null || target == null) {
      return target;
    }

    for (String key : source.getAsJsonObject().keySet()) {
      JsonElement sourceValue = source.get(key);
      if (!target.has(key)) {
        target.add(key, sourceValue);
      } else {
        JsonElement targetValue = target.get(key);
        if (sourceValue instanceof JsonArray && targetValue instanceof JsonArray) {
          target.get(key).getAsJsonArray().addAll(sourceValue.getAsJsonArray());
        } else if (sourceValue instanceof JsonObject && targetValue instanceof JsonObject) {
          target
              .getAsJsonObject()
              .add(key, deepMerge(sourceValue.getAsJsonObject(), targetValue.getAsJsonObject()));
        } else {
          target.add(key, sourceValue);
        }
      }
    }
    return target;
  }

  /**
   * jsonToYaml.
   *
   * @param jsonString a {@link java.lang.String} object
   * @return a {@link java.lang.String} object
   * @since 1.0.8
   */
  public static String jsonToYaml(String jsonString) {
    try {
      JsonNode json = new ObjectMapper().readTree(jsonString);
      return new YAMLMapper(
              new YAMLFactory()
                  .enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE)
                  .enable(YAMLGenerator.Feature.SPLIT_LINES))
          .writeValueAsString(json);
    } catch (JsonProcessingException jsonProcessingException) {
      throw new CoreException(jsonProcessingException);
    }
  }

  /**
   * csvToJson.
   *
   * @param input a {@link java.lang.String} object
   * @return a {@link java.lang.String} object
   * @since 1.0.8
   */
  public static String csvToJson(String input) {
    JSONArray json = CDL.toJSONArray(input);
    return json.toString();
  }

  /**
   * jsonToCsv.
   *
   * @param data a {@link com.google.gson.JsonObject} object
   * @return a {@link java.lang.String} object
   * @since 1.0.8
   */
  public static String jsonToCsv(JsonObject data) {
    JSONArray jsonArray = new JSONArray(data.getAsJsonArray("myArrayList").toString());
    return CDL.toString(jsonArray);
  }

  /**
   * createAndWriteJsonToExcel.
   *
   * @param resourcePath a {@link java.lang.String} object
   * @param data a {@link com.google.gson.JsonObject} object
   * @return a {@link java.net.URL} object
   * @since 1.0.8
   */
  public static URL createAndWriteJsonToExcel(String resourcePath, JsonObject data) {

    try (Workbook workbook =
        resourcePath.contains(".xlsx") ? new XSSFWorkbook() : new HSSFWorkbook()) {
      for (String sheetName : data.keySet()) {
        JsonArray sheetData = data.getAsJsonArray(sheetName);
        Sheet sheet = workbook.createSheet(sheetName);

        Row headerRow = sheet.createRow(0);
        JsonObject firstRowData = sheetData.get(0).getAsJsonObject();
        int cellIndex = 0;

        for (String columnHeader : firstRowData.keySet()) {
          Cell cell = headerRow.createCell(cellIndex);
          cell.setCellValue(columnHeader);
          cellIndex++;
        }
        for (int i = 0; i < sheetData.size(); i++) {
          Row dataRow = sheet.createRow(i + 1);
          cellIndex = 0;
          JsonObject rowData = sheetData.get(i).getAsJsonObject();

          for (String columnHeader : rowData.keySet()) {
            Cell cell = dataRow.createCell(cellIndex);
            JsonElement cellValue = rowData.get(columnHeader);
            cell.setCellValue(cellValue.isJsonNull() ? "" : cellValue.getAsString());
            cellIndex++;
          }
        }
      }
      File file = createFileIfItDoesNotExist(resourcePath);
      try (FileOutputStream fileOut = new FileOutputStream(file)) {
        workbook.write(fileOut);
      }
      return file.toURI().toURL();
    } catch (IOException e) {
      throw new CoreException("Excel writing failed due to exception: " + e.getMessage());
    }
  }

  /**
   * toHtmlDocument.
   *
   * @param html a {@link java.lang.String} object
   * @return a {@link org.jsoup.nodes.Document} object
   * @since 1.0.8
   */
  public static Document toHtmlDocument(String html) {
    return Jsoup.parse(html);
  }

  /**
   * find.
   *
   * @param input a {@link java.lang.String} object
   * @param regex a {@link java.lang.String} object
   * @return a {@link java.util.List} object
   * @since 1.0.8
   */
  public static List<String> find(String input, String regex) {
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(input);

    List<String> matches = new ArrayList<>();
    while (matcher.find()) {
      matches.add(matcher.group());
    }
    return matches;
  }

  /**
   * getDataType.
   *
   * @param inputString a {@link java.lang.String} object
   * @return a {@link org.autong.enums.DataType} object
   * @since 1.0.8
   */
  public static DataType getDataType(String inputString) {
    if (StringUtils.isNotEmpty(inputString)) {
      for (DataType type : DataType.values()) {
        try {
          read(IOUtils.toInputStream(inputString.trim()), type);
          return type;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    return null;
  }

  /**
   * decodeBase64.
   *
   * @param inputBase64Format a {@link java.lang.String} object
   * @return an array of {@link byte} objects
   * @since 1.0.8
   */
  public static byte[] decodeBase64(String inputBase64Format) {
    try {
      return BaseEncoding.base64().decode(inputBase64Format);
    } catch (Exception e) {
      return BaseEncoding.base64Url().decode(inputBase64Format);
    }
  }

  /**
   * encodeBase64.
   *
   * @param bytes an array of {@link byte} objects
   * @return a {@link java.lang.String} object
   * @since 1.0.8
   */
  public static String encodeBase64(byte[] bytes) {

    try {
      return BaseEncoding.base64().encode(bytes);
    } catch (Exception e) {
      return BaseEncoding.base64Url().encode(bytes);
    }
  }

  private static JsonObject excelToJson(String filePath) throws IOException {
    try (FileInputStream excelFile = new FileInputStream(filePath)) {
      Workbook workbook =
          filePath.endsWith(".xlsx") ? new XSSFWorkbook(excelFile) : new HSSFWorkbook(excelFile);
      return excelToJson(workbook);
    } catch (IOException ex) {
      throw new CoreException(ex);
    }
  }

  private static JsonObject excelToJson(InputStream stream) {
    try {
      XSSFWorkbook workbook = new XSSFWorkbook(stream);
      return excelToJson(workbook);
    } catch (IOException ex) {
      throw new CoreException(ex);
    }
  }

  private static JsonObject excelToJson(Workbook workbook) {
    FormulaEvaluator formulaEvaluator =
        (workbook instanceof XSSFWorkbook)
            ? new XSSFFormulaEvaluator((XSSFWorkbook) workbook)
            : new HSSFFormulaEvaluator((HSSFWorkbook) workbook);
    JsonObject workbookJson = new JsonObject();

    for (Sheet sheet : workbook) {
      JsonArray sheetJson = new JsonArray();
      int lastRowNum = sheet.getLastRowNum();
      int lastColumnNum = sheet.getRow(0).getLastCellNum();
      Row firstRowAsKeys = sheet.getRow(0);

      for (int i = 1; i <= lastRowNum; i++) {
        JsonObject rowJson = new JsonObject();
        Row row = sheet.getRow(i);

        if (row != null) {
          for (int j = 0; j < lastColumnNum; j++) {
            formulaEvaluator.evaluate(row.getCell(j));
            rowJson.addProperty(
                cellToString(firstRowAsKeys.getCell(j)),
                new DataFormatter().formatCellValue(row.getCell(j), formulaEvaluator));
          }
          sheetJson.add(rowJson);
        }
      }
      workbookJson.add(sheet.getSheetName(), sheetJson);
    }

    return workbookJson;
  }

  private static JsonObject pdfToJson(URL filePath) throws IOException {
    File file = new File(filePath.getPath());
    return pdfToJson(PDDocument.load(file));
  }

  private static JsonObject pdfToJson(byte[] bytes) throws IOException {
    return pdfToJson(PDDocument.load(bytes));
  }

  private static JsonObject pdfToJson(PDDocument document) throws IOException {
    PDFTextStripper pdfStripper = new PDFTextStripper();
    String text = pdfStripper.getText(document);
    JsonObject pdfData = new JsonObject();
    pdfData.addProperty("data", text);
    return pdfData;
  }

  private static JsonObject extendData(URL resourceUrl, JsonObject data, DataType dataType) {
    String extendsKey = "$extends";
    if (data != null && data.has(extendsKey)) {

      JsonObject baseData;
      if (resourceUrl != null && resourceUrl.getProtocol().equals("jar")) {
        try {
          String jarPath = resourceUrl.toString().split("!")[0];
          String resourcePath = jarPath + "!/" + data.get(extendsKey).getAsString();
          baseData = read(new URL(resourcePath), dataType);
        } catch (MalformedURLException ex) {
          throw new CoreException(ex);
        }
      } else {
        baseData = read(data.get(extendsKey).getAsString(), dataType);
      }

      data.remove(extendsKey);
      data = deepMerge(data, baseData);
    }

    return data;
  }

  private static File createFileIfItDoesNotExist(String pathWithFilename) {
    File file;
    if (new File(pathWithFilename).isAbsolute()) {
      file = new File(pathWithFilename);
    } else {
      file = new File("target/" + pathWithFilename);
    }

    if (!file.exists()) {
      try {
        Files.createParentDirs(file);
        file.createNewFile();
      } catch (IOException e) {
        throw new CoreException("An error occurred while creating the file: " + e.getMessage());
      }
    }
    return file;
  }

  private static URL createAndWriteToFile(String pathWithFilename, String data) {
    File file = createFileIfItDoesNotExist(pathWithFilename);
    try (FileWriter writer = new FileWriter(file)) {
      writer.write(data);
      return file.toURI().toURL();
    } catch (IOException e) {
      throw new CoreException(
          "An error occurred while writing data to the file: " + e.getMessage());
    }
  }

  private static String cellToString(Cell cell) {
    switch (cell.getCellType()) {
      case STRING:
        return cell.getStringCellValue();
      case NUMERIC:
        if (DateUtil.isCellDateFormatted(cell)) {
          return cell.getDateCellValue().toString();
        } else {
          return String.valueOf(cell.getNumericCellValue());
        }
      case BOOLEAN:
        return String.valueOf(cell.getBooleanCellValue());
      default:
        return "";
    }
  }
}
