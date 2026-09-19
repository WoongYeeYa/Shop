<%@ page pageEncoding="UTF-8" %>
<%@ include file="header.jspf" %>
<p class="eyebrow">WE'RE HERE TO HELP</p><h1>무엇이 궁금하세요?</h1><p class="muted">담당자가 내용을 확인한 후 답변해 드립니다. 답변은 내 문의에서 확인할 수 있어요.</p>
<form method="post" class="support-panel support-form" action="${pageContext.request.contextPath}/account/inquiry/new" data-submit-lock>
<input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
<div class="form-columns"><div><label for="category">문의 유형</label><select id="category" name="category" required>
<option value="PRODUCT" ${param.category == 'PRODUCT' ? 'selected' : ''}>상품 정보</option><option value="ORDER" ${param.category == 'ORDER' ? 'selected' : ''}>주문·배송</option><option value="RETURN" ${param.category == 'RETURN' ? 'selected' : ''}>교환·반품</option><option value="OTHER" ${param.category == 'OTHER' ? 'selected' : ''}>기타</option></select></div>
<div><label for="productId">관련 상품 <small>선택</small></label><select id="productId" name="productId"><option value="">선택하지 않음</option><c:forEach var="p" items="${products}"><option value="${p.id}" ${param.productId == p.id ? 'selected' : ''}><c:out value="${p.name}"/></option></c:forEach></select></div></div>
<label for="orderId">관련 주문 <small>선택 · 본인 주문만 표시됩니다</small></label><select id="orderId" name="orderId"><option value="">선택하지 않음</option><c:forEach var="o" items="${orders}"><option value="${o.id}" ${param.orderId == o.id ? 'selected' : ''}><c:out value="${o.orderNumber}"/> · <fmt:formatDate value="${o.createdAt}" pattern="yyyy.MM.dd"/></option></c:forEach></select>
<p class="help">상품과 주문을 함께 선택한다면 해당 주문에 포함된 상품을 선택해 주세요.</p>
<label for="title">제목</label><input id="title" name="title" maxlength="120" required value="<c:out value='${param.title}'/>" placeholder="궁금한 내용을 한 줄로 알려 주세요">
<label for="body">문의 내용</label><textarea id="body" name="body" rows="8" maxlength="4000" required placeholder="상황을 자세히 적어 주시면 정확한 안내에 도움이 됩니다."><c:out value="${param.body}"/></textarea>
<p class="help">비밀번호·카드번호 등 민감한 정보는 입력하지 마세요.<c:if test="${aiEnabled}"> 답변 초안 작성을 위해 문의와 관련 상품·주문 상태가 외부 AI 서비스에 전달됩니다.</c:if></p>
<div class="form-actions"><a href="${pageContext.request.contextPath}/account/inquiries">목록으로</a><button class="support-button" type="submit">문의 접수하기 →</button></div>
</form></main></body></html>
