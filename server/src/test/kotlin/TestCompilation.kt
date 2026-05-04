// This file is just to test if all imports work correctly
package test

// Test imports from all new packages
import com.lucasalfare.flpoint.server.domain.model.User
import com.lucasalfare.flpoint.server.domain.model.Point
import com.lucasalfare.flpoint.server.domain.model.CreateUserRequestDTO
import com.lucasalfare.flpoint.server.domain.model.CredentialsDTO
import com.lucasalfare.flpoint.server.domain.model.UserDTO
import com.lucasalfare.flpoint.server.domain.model.PointDTO
import com.lucasalfare.flpoint.server.domain.repository.DataCRUD
import com.lucasalfare.flpoint.server.domain.validation.validateName
import com.lucasalfare.flpoint.server.domain.validation.validateEmail
import com.lucasalfare.flpoint.server.domain.validation.validatePassword
import com.lucasalfare.flpoint.server.domain.validation.instantIsAtLeast10SecondsAwayFromLast
import com.lucasalfare.flpoint.server.infrastructure.persistence.ExposedDataCRUD
import com.lucasalfare.flpoint.server.infrastructure.persistence.AppDB
import com.lucasalfare.flpoint.server.infrastructure.persistence.Users
import com.lucasalfare.flpoint.server.infrastructure.persistence.Points
import com.lucasalfare.flpoint.server.infrastructure.persistence.JwtBlacklist
import com.lucasalfare.flpoint.server.infrastructure.security.JwtGenerator
import com.lucasalfare.flpoint.server.infrastructure.security.AppJwtClaims
import com.lucasalfare.flpoint.server.infrastructure.security.PasswordHashing
import com.lucasalfare.flpoint.server.infrastructure.security.JwtUtils
import com.lucasalfare.flpoint.server.infrastructure.security.Authentication
import com.lucasalfare.flpoint.server.application.usecase.AppUsecases
import com.lucasalfare.flpoint.server.application.route.routesHandlers
import com.lucasalfare.flpoint.server.config.initializeDatabase
import com.lucasalfare.flpoint.server.config.initKtorConfiguration
import com.lucasalfare.flpoint.server.shared.Constants
import com.lucasalfare.flpoint.server.shared.DataHandlingError
import com.lucasalfare.flpoint.server.shared.AuthenticationError
import com.lucasalfare.flpoint.server.shared.ValidationError
import com.lucasalfare.flpoint.server.shared.RuleViolatedError
import com.lucasalfare.flpoint.server.shared.NoPrivilegeError
import com.lucasalfare.flpoint.server.shared.AppError
import com.lucasalfare.flpoint.server.shared.customRootCause

// Simple test function to verify imports work
fun testImports() {
    println("All imports compiled successfully!")
}
