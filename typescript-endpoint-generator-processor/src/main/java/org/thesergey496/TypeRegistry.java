package org.thesergey496;

import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.thesergey496.annotations.TypeMapping;
import org.thesergey496.processors.GeneratedTypeProcessor;
import org.thesergey496.processors.TypeProcessor;
import org.thesergey496.processors.impl.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;

@RequiredArgsConstructor
public class TypeRegistry {
    protected final Elements elementUtils;
    protected final Types typeUtils;

    protected final Map<TypeMirror, TypeMapping> exactMatches = new HashMap<>();
    protected final SequencedMap<TypeMirror, TypeMapping> hierarchyMatches = new LinkedHashMap<>();

    protected final Map<TypeMirror, TypeProcessor> typeProcessors = new HashMap<>();

    public Set<TypeProcessor> getTypeProcessors() {
        return new HashSet<>(this.typeProcessors.values());
    }

    public void addRule(TypeMirror typeMirror, TypeMapping targetType) {
        if (targetType.includeSubclasses()) {
            hierarchyMatches.put(typeUtils.erasure(typeMirror), targetType);
        } else {
            exactMatches.put(typeUtils.erasure(typeMirror), targetType);
        }
    }

    public TypeProcessor resolve(TypeMirror typeToCheck) {
        if (this.typeProcessors.containsKey(typeUtils.erasure(typeToCheck))) {
            return this.typeProcessors.get(typeUtils.erasure(typeToCheck));
        }
        final TypeProcessor typeProcessor = this.resolveInternal(typeToCheck);
        this.typeProcessors.put(typeToCheck, typeProcessor);
        return typeProcessor;
    }

    protected TypeProcessor resolveInternal(TypeMirror typeToCheck) {
        if (typeToCheck.getKind() == TypeKind.TYPEVAR) {
            return new PrimitiveTypeProcessor(typeToCheck.toString());
        }
        if (typeToCheck.getKind() == TypeKind.ARRAY) {
            return new CollectionTypeProcessor();
        }
        if (typeToCheck.getKind().isPrimitive()) {
            if (typeToCheck.getKind() == TypeKind.BOOLEAN) {
                return PrimitiveTypeProcessor.BOOLEAN;
            } else {
                return PrimitiveTypeProcessor.NUMBER;
            }
        }


        final TypeMirror type = typeUtils.erasure(typeToCheck);
        if (exactMatches.containsKey(type)) {
            return getTypeProcessor(exactMatches.get(type));
        }

        for (final Map.Entry<TypeMirror, TypeMapping> entry : hierarchyMatches.reversed().entrySet()) {
            final TypeMirror parentType = entry.getKey();

            if (typeUtils.isSubtype(type, parentType)) {
                return getTypeProcessor(entry.getValue());
            }
        }

        try {
            return createGeneratedTypeProcessor(typeToCheck);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return PrimitiveTypeProcessor.ANY;
    }

    protected @NonNull GeneratedTypeProcessor createGeneratedTypeProcessor(TypeMirror typeToCheck) {
        final GeneratedTypeProcessor generatedTypeProcessor;
        final Element element = typeUtils.asElement(typeToCheck);
        if (element.getKind() == ElementKind.ENUM) {
            generatedTypeProcessor = new EnumTypeProcessor(element);
        } else {
            final DtoTypeProcessor dtoTypeProcessor = new DtoTypeProcessor(element);
            this.typeProcessors.put(typeUtils.erasure(typeToCheck), dtoTypeProcessor);
            dtoTypeProcessor.init(element);
            generatedTypeProcessor = dtoTypeProcessor;
        }
        return generatedTypeProcessor;
    }

    protected TypeProcessor getTypeProcessor(TypeMapping targetType) {
        return switch (targetType.mode()) {
            case COLLECTION -> new CollectionTypeProcessor();
            case MAP -> new MapTypeProcessor();
            case PRIMITIVE -> new PrimitiveTypeProcessor(targetType.target());
            case ALIAS -> {
                final TypeMirror mirror = TypeMirrorHelper.getTypeMirrorFromTypeMapping(targetType, elementUtils, typeUtils);
                final String fullName = mirror.toString();
                final String name = fullName.substring(fullName.lastIndexOf(".") + 1);
                yield new AliasTypeProcessor(name, new PrimitiveTypeProcessor(targetType.target()));
            }
            case CUSTOM -> {
                final TypeMirror mirror = TypeMirrorHelper.getTypeMirrorFromTypeMapping(targetType, elementUtils, typeUtils);
                final String fullName = mirror.toString();
                final String name = fullName.substring(fullName.lastIndexOf(".") + 1);
                yield new CustomTypeProcessor(name, targetType.target());
            }
        };
    }
}