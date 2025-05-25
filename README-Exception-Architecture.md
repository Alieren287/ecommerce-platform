# Simplified Categorized Exception Architecture with Default Messages

## Overview

This project implements a simplified categorized exception architecture with **embedded default messages** in error codes. This approach provides a direct relationship between error codes and their messages while maintaining flexibility for custom messages when needed.

## Exception Hierarchy

```
BaseBusinessException (abstract)
├── BusinessException (general business logic violations → HTTP 400)
├── ValidationException (input validation errors → HTTP 400)  
├── ResourceNotFoundException (resource not found → HTTP 404)
├── ConflictException (business conflicts → HTTP 409)
└── InternalServerException (system errors → HTTP 500)
```

## Core Components

### 1. Exception Classes (in `ecommerce-core`)

- **`BaseBusinessException`**: Abstract base class for all business exceptions
- **`BusinessException`**: General business logic violations → HTTP 400 Bad Request
- **`ValidationException`**: Input validation errors → HTTP 400 Bad Request
- **`ResourceNotFoundException`**: Resource not found errors → HTTP 404 Not Found  
- **`ConflictException`**: Business rule conflicts → HTTP 409 Conflict
- **`InternalServerException`**: System/technical errors → HTTP 500 Internal Server Error

### 2. Error Code Interface

- **`ErrorCode`**: Interface requiring `getCode()` and `getDefaultMessage()` methods

### 3. Exception Handler

The `GlobalRestExceptionHandler` automatically maps each exception type to its fixed HTTP status and response format.

## Key Innovation: Embedded Default Messages

Error codes now contain their default messages directly, creating a **type-safe relationship** between codes and messages:

```java
public enum ProductErrorCode implements ErrorCode {
    PRODUCT_NOT_FOUND("ERR-PRD-001", "Product not found with the specified identifier"),
    PRODUCT_NAME_NULL("ERR-PRD-004", "Product name is required and cannot be null"),
    PRODUCT_SKU_EXISTS("ERR-PRD-002", "A product with this SKU already exists in the system");
    
    private final String code;
    private final String defaultMessage;
}
```

## Usage Examples

### Using Default Messages (Recommended for 90% of cases)

```java
// Clean and simple - uses default messages from error codes
throw ProductException.validation(ProductErrorCode.PRODUCT_NAME_NULL);
// Message: "Product name is required and cannot be null"

throw ProductException.notFound(ProductErrorCode.PRODUCT_NOT_FOUND);
// Message: "Product not found with the specified identifier"

throw ProductException.conflict(ProductErrorCode.PRODUCT_SKU_EXISTS);
// Message: "A product with this SKU already exists in the system"
```

### Custom Message Overrides (For special contexts)

```java
// When you need specific context or dynamic values
throw ProductException.validation(ProductErrorCode.PRODUCT_NAME_NULL, 
    "Product name is required for bulk import operation");

throw ProductException.notFound(ProductErrorCode.PRODUCT_NOT_FOUND, 
    "Product with SKU '" + sku + "' was not found in catalog");

throw ProductException.conflict(ProductErrorCode.PRODUCT_SKU_EXISTS, 
    "Cannot update product: SKU '" + newSku + "' already exists");
```

### Direct Exception Usage

```java
// Using default messages
throw new ValidationException(ProductErrorCode.PRODUCT_NAME_NULL);

// Using custom messages
throw new ValidationException(ProductErrorCode.PRODUCT_NAME_NULL, "Custom validation message");
```

## Domain-Specific Factory Pattern

```java
public class ProductException {
    // Factory methods using default messages
    public static ValidationException validation(ProductErrorCode errorCode) {
        return new ValidationException(errorCode, errorCode.getDefaultMessage());
    }
    
    public static ResourceNotFoundException notFound(ProductErrorCode errorCode) {
        return new ResourceNotFoundException(errorCode, errorCode.getDefaultMessage());
    }
    
    // Factory methods with custom message overrides
    public static ValidationException validation(ProductErrorCode errorCode, String customMessage) {
        return new ValidationException(errorCode, customMessage);
    }
    
    public static ResourceNotFoundException notFound(ProductErrorCode errorCode, String customMessage) {
        return new ResourceNotFoundException(errorCode, customMessage);
    }
    
    // ... other factory methods
}
```

## Creating Module-Specific Error Codes

Each module creates error code enums with embedded default messages:

```java
@RequiredArgsConstructor
@Getter
public enum ProductErrorCode implements ErrorCode {
    // Not Found errors
    PRODUCT_NOT_FOUND("ERR-PRD-001", "Product not found with the specified identifier"),
    
    // Validation errors
    PRODUCT_NAME_NULL("ERR-PRD-004", "Product name is required and cannot be null"),
    PRODUCT_PRICE_NEGATIVE("ERR-PRD-008", "Product price cannot be negative. Please provide a valid positive price"),
    
    // Conflict errors
    PRODUCT_SKU_EXISTS("ERR-PRD-002", "A product with this SKU already exists in the system"),
    PRODUCT_OUT_OF_STOCK("ERR-PRD-003", "Product is currently out of stock and cannot be purchased"),
    
    // Internal server errors
    PRODUCT_UPDATE_FAILED("ERR-PRD-030", "Product update operation failed due to a system error. Please try again");

    private final String code;
    private final String defaultMessage;
}
```

## Benefits of This Approach

1. **Type Safety**: Direct relationship between error codes and messages - no mismatched codes/messages
2. **Simplicity**: Default messages for 90% of use cases - just specify the error code
3. **Flexibility**: Custom message overrides for special contexts (10% of cases)
4. **Maintainability**: No separate message classes needed - everything in one place
5. **Compile-time Safety**: Impossible to have orphaned messages or missing codes
6. **Clean Code**: Most exception throwing becomes one-liners
7. **Consistency**: Same error code always has the same default message across the application

## When to Use Custom Messages

Use custom messages when you need:
- **Dynamic values**: Including specific IDs, names, or other runtime data
- **Context-specific wording**: Different operations need different explanations
- **User-facing vs. internal messages**: Different audiences need different detail levels
- **Localization**: Different languages or regions

## HTTP Status Mapping

Each exception type has a fixed HTTP status:

| Exception Type | HTTP Status | Usage |
|----------------|-------------|-------|
| `ValidationException` | 400 Bad Request | Input validation failures |
| `BusinessException` | 400 Bad Request | General business rule violations |
| `ResourceNotFoundException` | 404 Not Found | Resource not found |
| `ConflictException` | 409 Conflict | Business conflicts (e.g., duplicate SKU) |
| `InternalServerException` | 500 Internal Server Error | System/technical errors |

## Migration from Separate Message Classes

1. **Move messages into error code enums** as default messages
2. **Update ErrorCode interface** to include `getDefaultMessage()`
3. **Create overloaded factory methods** for default and custom messages
4. **Update existing code** to use default messages where appropriate
5. **Remove separate message classes** (e.g., ProductErrorMessages)

## Best Practices

1. **Use default messages for standard cases** - keeps code clean and consistent
2. **Use custom messages for dynamic content** - when you need specific IDs, names, etc.
3. **Write clear default messages** - they should be user-friendly and actionable
4. **Keep custom messages contextual** - explain why the error occurred in this specific situation
5. **Maintain consistency** - same error code should represent the same logical error across the application

## Testing Exception Handling

```java
// Test with default message
ValidationException exception = assertThrows(ValidationException.class, () -> {
    // Code that should throw exception
});
assertEquals(ProductErrorCode.PRODUCT_NAME_NULL, exception.getErrorCode());
assertEquals("Product name is required and cannot be null", exception.getMessage());

// Test with custom message
ValidationException customException = assertThrows(ValidationException.class, () -> {
    throw ProductException.validation(ProductErrorCode.PRODUCT_NAME_NULL, "Custom message");
});
assertEquals("Custom message", customException.getMessage());
```

## Module Structure

- **`ecommerce-core`**: Contains all exception classes and ErrorCode interface
- **`ecommerce-web-core`**: Contains only GlobalRestExceptionHandler
- **Domain modules**: Implement ErrorCode enums with default messages and create factory classes 