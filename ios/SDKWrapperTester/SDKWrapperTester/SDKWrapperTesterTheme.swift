/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

import Foundation
import SwiftUI

open class SDKWrapperTesterTheme: ObservableObject {
	var itemSpacing: CGFloat = 24

	var iconSizeSmall: CGFloat = 20
	var iconSizeLarge: CGFloat = 96

	open var textAppearanceButton: TextAppearance {
		TextAppearance(font: .body, color: .accentColor)
	}

	open var textAppearanceTitle: TextAppearance {
		TextAppearance(font: .headline, color: .primary)
	}

	open var textAppearanceBody: TextAppearance {
		TextAppearance(font: .body, color: .primary)
	}

	open var textAppearanceListTitle: TextAppearance {
		TextAppearance(font: .footnote, color: .secondary)
	}

	open var textAppearanceListItem: TextAppearance {
		TextAppearance(font: .body, color: .secondary)
	}

	open var textAppearanceListItemCaption: TextAppearance {
		TextAppearance(font: .body, color: .primary)
	}

	open var textAppearanceWarning: TextAppearance {
		TextAppearance(font: .body, color: .red)
	}

	open var errorIcon: AnyView {
		Image(systemName: "exclamationmark.circle")
			.resizable()
			.colorMultiply(.primary)
			.typeErase()
	}

	open var passwordErrorIcon: AnyView {
		Image(systemName: "exclamationmark.circle.fill")
			.resizable()
			.foregroundColor(.red)
			.typeErase()
	}

	open var passwordHiddenIcon: AnyView {
		Image(systemName: "eye.slash.fill")
			.resizable()
			.colorMultiply(.primary)
			.typeErase()
	}

	open var passwordVisibleIcon: AnyView {
		Image(systemName: "eye.fill")
			.resizable()
			.colorMultiply(.primary)
			.typeErase()
	}

	open var cardRequestedIcon: AnyView {
		Image(systemName: "dot.radiowaves.left.and.right")
			.resizable()
			.colorMultiply(.primary)
			.typeErase()
	}

	open var loadCertificateIcon: AnyView {
		Image(systemName: "doc.text")
			.resizable()
			.colorMultiply(.primary)
			.typeErase()
	}

	open var enterPasswordIcon: AnyView {
		Image(systemName: "lock")
			.resizable()
			.colorMultiply(.primary)
			.typeErase()
	}

	open var authenticationAbortedIcon: AnyView {
		Image(systemName: "info.circle")
			.resizable()
			.colorMultiply(.primary)
			.typeErase()
	}

	open var enterNewPasswordIcon: AnyView {
		Image(systemName: "lock.open")
			.resizable()
			.colorMultiply(.primary)
			.typeErase()
	}
}

public struct TextAppearance {
	let font: Font
	let color: Color

	public init(font: Font, color: Color) {
		self.font = font
		self.color = color
	}
}

private extension View {
	func typeErase() -> AnyView {
		return AnyView(self)
	}
}
